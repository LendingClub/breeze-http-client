/*
 * Copyright (C) 2018 Lending Club, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.lendingclub.http.breeze.client.impl.jaxrs.proxy;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.FormParam;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

import org.lendingclub.http.breeze.client.BreezeHttpClient;
import org.lendingclub.http.breeze.client.BreezeHttpRequest;
import org.lendingclub.http.breeze.client.BreezeHttpType;
import org.lendingclub.http.breeze.client.exception.BreezeHttpException;

/**
 *
 * @author vreddy
 *
 */
public class BreezeWebResourceFactory implements InvocationHandler {
    private final Map<String, String> headers = new HashMap<String, String>();
    private final BreezeHttpClient breezeHttpClient;
    private final UrlProvider urlProvider;

    private ContentTypeHeaderHelper contentHeaderHelper = new ContentTypeHeaderHelper();

    /**
     * @param breezeHttpClient
     * @param headers
     * @param rootUrlProvider
     */
    public BreezeWebResourceFactory(BreezeHttpClient breezeHttpClient, Map<String, String> headers,
                                    UrlProvider rootUrlProvider) {
        this.breezeHttpClient = breezeHttpClient;
        if (headers != null) {
            this.headers.putAll(headers);
        }
        this.urlProvider = rootUrlProvider;
    }

    @SuppressWarnings("unchecked")
    public static <C> C newResource(final Class<C> clazz, final BreezeHttpClient breezeHttpClient,
            final Map<String, String> headers, UrlProvider urlProvider) {
        return (C) Proxy.newProxyInstance(clazz.getClassLoader(), new Class[] {clazz},
                new BreezeWebResourceFactory(breezeHttpClient, headers, urlProvider));
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if (method.getName().equals("equals")) {
            return proxy == args[0];
        } else if (method.getName().equals("hashCode")) {
            return System.identityHashCode(proxy);
        } else if (method.getName().equals("toString")) {
            return proxy.getClass().getName() + '@' + Integer.toHexString(proxy.hashCode());
        }
        final Annotation[][] parameterAnnotations = method.getParameterAnnotations();
        // Object entity = null;
        Form form = null;
        Object entity = null;
        BreezeHttpRequest breezeHttpRequest = new BreezeHttpRequest(this.urlProvider.getRootUrl(), breezeHttpClient);

        // Pre-allocate this object in case it is needed.
        FormPayloadBuilder formPayloadBuilderRequest = new FormPayloadBuilder(breezeHttpRequest.form());

        // get the interface describing the resource
        final Class<?> proxyInterfaceClass = proxy.getClass().getInterfaces()[0];
        breezeHttpRequest.headers(this.headers);
        for (int i = 0; i < parameterAnnotations.length; i++) {
            final Map<Class<?>, Annotation> anns = new HashMap<Class<?>, Annotation>();
            for (final Annotation ann : parameterAnnotations[i]) {
                anns.put(ann.annotationType(), ann);
            }
            Object value = args[i];
            if (anns.isEmpty()) {
                if (value instanceof Form) {
                    form = (Form) value;
                } else {
                    entity = value;
                }
            } else {
                if (value == null) {
                    if (anns.get(DefaultValue.class) != null) {
                        value = ((DefaultValue) anns.get(DefaultValue.class)).value();
                    }
                }
                if (value != null) {
                    if (anns.get(PathParam.class) != null) {
                        breezeHttpRequest.pathVariable(((PathParam) anns.get((PathParam.class))).value(), value);
                    } else if (anns.get((QueryParam.class)) != null) {
                        breezeHttpRequest.queryVariable(((QueryParam) anns.get((QueryParam.class))).value(), value);
                    } else if (anns.get((HeaderParam.class)) != null) {
                        breezeHttpRequest.header(((HeaderParam) anns.get((HeaderParam.class))).value(),
                                String.valueOf(value));
                    } else if (anns.get((FormParam.class)) != null) {
                        if (value instanceof Collection) {
                            for (final Object v : ((Collection<?>) value)) {
                                formPayloadBuilderRequest.param(((FormParam) anns.get((FormParam.class))).value(),
                                        v.toString());
                            }
                        } else {
                            formPayloadBuilderRequest.param(((FormParam) anns.get((FormParam.class))).value(),
                                    value.toString());
                        }
                    }
                }
            }
        }

        if (form != null) {
            for (Entry<String, List<String>> entrySet : form.asMap().entrySet()) {
                List<String> multiValue = entrySet.getValue();
                String key = entrySet.getKey();
                for (final String v : multiValue) {
                    formPayloadBuilderRequest.param(key, v);
                }
            }
        }

        breezeHttpRequest.header(HttpHeaders.CONTENT_TYPE, getContentType(method, form, proxyInterfaceClass));

        breezeHttpRequest.header(HttpHeaders.ACCEPT, getAccepts(method, proxyInterfaceClass));

        String httpMethod = getHttpMethodName(method);
        if (httpMethod == null) {
            for (final Annotation ann : method.getAnnotations()) {
                httpMethod = getHttpMethodName(ann.annotationType());
                if (httpMethod != null) {
                    break;
                }
            }
        }
        if (httpMethod == null) {
            throw new BreezeHttpException(String.format("HTTPMethod not found on interface method=%s", method.getName()));
        }
        breezeHttpRequest.method(httpMethod);
        String methodUri = addPathFromAnnotation(method);
        String pathUri = methodUri;
        String classPathUri = null;
        Path path = proxyInterfaceClass.getAnnotation(Path.class);
        if (path != null) {
            classPathUri = path.value();
        }
        if (classPathUri != null) {
            pathUri = new StringBuffer().append(classPathUri).append(methodUri).toString();
        } else {
            pathUri = methodUri;
        }
        breezeHttpRequest.path(pathUri);
        Class<?> responseType = method.getReturnType();
        /**
         * Support for generics in response type . #getGenericReturnType() will
         * return Void if the response is not generic
         */
        Type genericReturnType = method.getGenericReturnType();
        if (genericReturnType == responseType) {
            genericReturnType = null;
        }

        if (null == entity && formPayloadBuilderRequest.hasParameters()) {
            entity = formPayloadBuilderRequest;
        }

        Optional<Object> returnData = execute(breezeHttpRequest, responseType, entity, genericReturnType);
        Object response = null;
        if (returnData.isPresent()) {
            response = returnData.get();
        }
        return response;
    }

    /**
     * @param method
     * @param proxyInterfaceClass
     * @return
     */
    protected String getAccepts(Method method, final Class<?> proxyInterfaceClass) {
        String accepts = contentHeaderHelper.getAcceptsHeader(proxyInterfaceClass, method);

        if (null == accepts) {
            accepts = MediaType.APPLICATION_JSON;
        }

        return accepts;
    }

    /**
     * @param method
     * @param form
     * @param proxyInterfaceClass
     * @return
     */
    protected String getContentType(Method method, Form form, final Class<?> proxyInterfaceClass) {
        String contentType = contentHeaderHelper.getContentTypeHeader(proxyInterfaceClass, method);

        if (null == contentType) {
            if (form != null) {
                contentType = MediaType.APPLICATION_FORM_URLENCODED;
            } else {
                // Assume we are sending JSON
                contentType = MediaType.APPLICATION_JSON;
            }
        }

        return contentType;
    }

    protected Optional<Object> execute(BreezeHttpRequest breezeHttpRequest, Class<?> responseType, Object payLoad,
                                       Type parameterizedType) {
        Object response = null;

        BreezeHttpType<?> genericReturnType = (null != parameterizedType ? new BreezeHttpType<>(parameterizedType)
                : null);

        boolean voidReturnType = responseType.getName().equalsIgnoreCase("void");

        switch (breezeHttpRequest.getMethod()) {
        case POST:
            if (voidReturnType) {
                breezeHttpRequest.post(payLoad);
            } else if (genericReturnType != null) {
                response = breezeHttpRequest.post(genericReturnType, payLoad);
            } else {
                response = breezeHttpRequest.post(responseType, payLoad);
            }
            break;

        case PUT:
            if (voidReturnType) {
                breezeHttpRequest.put(payLoad);
            } else if (genericReturnType != null) {
                response = breezeHttpRequest.put(genericReturnType, payLoad);
            } else {
                response = breezeHttpRequest.put(responseType, payLoad);
            }
            break;

        case GET:
        default:
            if (voidReturnType) {
                breezeHttpRequest.get();
            } else if (genericReturnType != null) {
                response = breezeHttpRequest.get(genericReturnType);
            } else {
                response = breezeHttpRequest.get(responseType);
            }
            break;
        }

        if (response != null) {
            return Optional.of(response);
        }

        return Optional.empty();
    }

    private String getHttpMethodName(final AnnotatedElement ae) {
        final HttpMethod a = ae.getAnnotation(HttpMethod.class);
        return a == null ? null : a.value();
    }

    private String addPathFromAnnotation(final AnnotatedElement ae) {
        final Path p = ae.getAnnotation(Path.class);
        return p == null ? null : p.value();
    }

}
