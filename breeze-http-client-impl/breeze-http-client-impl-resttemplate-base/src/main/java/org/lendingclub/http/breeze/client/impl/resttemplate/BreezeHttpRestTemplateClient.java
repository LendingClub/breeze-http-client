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

package org.lendingclub.http.breeze.client.impl.resttemplate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.lendingclub.http.breeze.client.BreezeHttpRequest;
import org.lendingclub.http.breeze.client.BreezeHttpResponse;
import org.lendingclub.http.breeze.client.BreezeHttpType;
import org.lendingclub.http.breeze.client.exception.BreezeHttpException;
import org.lendingclub.http.breeze.client.filter.BreezeHttpRequestFilter;
import org.lendingclub.http.breeze.client.impl.AbstractBreezeHttpClient;
import org.lendingclub.http.breeze.client.impl.resttemplate.error.ClientErrorHandler;
import org.lendingclub.http.breeze.client.payload.BreezeRequestForm;

/**
 * RestTemplate implementation. Overrides RestTemplate's ResponseErrorHandler
 * so we can do special things like have the request object handy when we
 * process errors.
 *
 * @author Raul Acevedo
 */
public class BreezeHttpRestTemplateClient extends AbstractBreezeHttpClient {
    /** This is the only way to pass the original request to a RestTemplate error handler. */
    protected static final ThreadLocal<BreezeHttpRequest> REQUEST_CONTEXT = new ThreadLocal<>();

    protected final RestTemplate restTemplate;
    protected final ClientErrorHandler clientErrorHandler;

    public BreezeHttpRestTemplateClient(
            RestTemplate restTemplate,
            ClientErrorHandler clientErrorHandler,
            List<BreezeHttpRequestFilter> requestFilters
    ) {
        this(restTemplate,
                clientErrorHandler,
                requestFilters,
                LoggerFactory.getLogger(BreezeHttpRestTemplateClient.class));
    }

    public BreezeHttpRestTemplateClient(
            RestTemplate restTemplate,
            ClientErrorHandler clientErrorHandler,
            List<BreezeHttpRequestFilter> requestFilters,
            Logger logger
    ) {
        super(requestFilters, logger);
        this.restTemplate = restTemplate;
        this.restTemplate.setErrorHandler(new RestTemplateErrorHandler());
        this.clientErrorHandler = clientErrorHandler;
    }

    @Override
    public <T> BreezeHttpResponse<T> execute(BreezeHttpRequest request, BreezeHttpType<T> genericType, Object payload)
            throws BreezeHttpException {
        return invoke(request, genericType == null ? null : genericType.getType(), payload);
    }

    protected <T> BreezeHttpResponse<T> invoke(BreezeHttpRequest request, Type type, Object payload) {
        long startTime = System.currentTimeMillis();
        try {
            logRequestStart(request);
            REQUEST_CONTEXT.set(request);
            BreezeHttpResponse<T> response = exchange(request, type, payload);
            logRequestEnd(request, startTime);
            return response;
        } catch (BreezeHttpException e) {
            logRequestException(request, startTime, e);
            throw e;
        } catch (Exception e) {
            BreezeHttpException remoteException = clientErrorHandler.handleError(request, e);
            logRequestException(request, startTime, remoteException);
            throw remoteException;
        } finally {
            REQUEST_CONTEXT.remove();
        }
    }

    protected <T> BreezeHttpResponse<T> exchange(BreezeHttpRequest request, Type type, Object payload) {
        // Clever hack to pass generics type information which is normally erased at runtime
        ParameterizedTypeReference<T> typeReference = new ParameterizedTypeReference<T>() {
            @Override
            public Type getType() {
                return type;
            }
        };

        ResponseEntity<T> entity = restTemplate.exchange(
                buildURI(request),
                HttpMethod.valueOf(request.getMethod().toString()),
                createEntity(request, payload),
                typeReference
        );

        Map<String, List<String>> headers = new HashMap<>();
        entity.getHeaders().forEach((key, value) -> headers.put(key.toLowerCase(), new ArrayList<>(value)));

        return new BreezeHttpResponse<>(entity.getBody(), entity.getStatusCode().value(), headers);
    }

    protected URI buildURI(BreezeHttpRequest request) {
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(request.getRootUrl()).path(request.getPath());

        // Add query string variables
        if (!request.getQueryVariables().isEmpty()) {
            for (Map.Entry<String, List<Object>> queryVariable : request.getQueryVariables().entrySet()) {
                builder.queryParam(queryVariable.getKey(), queryVariable.getValue().toArray());
            }
        }

        return builder.buildAndExpand(request.getPathVariables()).encode().toUri();
    }

    private HttpEntity<?> createEntity(BreezeHttpRequest request, Object payload) {
        HttpHeaders headers = new HttpHeaders();
        headers.putAll(request.getHeaders());

        if (payload instanceof InputStream) {
            return new HttpEntity<>(new InputStreamResource((InputStream) payload), headers);
        } else if (payload instanceof BreezeRequestForm) {
            BreezeRequestForm form = (BreezeRequestForm) payload;
            MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
            form.params().forEach(map::add);
            return new HttpEntity<>(map, headers);
        } else {
            return new HttpEntity<>(payload, headers);
        }
    }

    /**
     * A custom RestTemplate error handler allows us to delegate all of our
     * error handling to a single class, and to pass in the original request
     * object to it. The request is important in constructing an exception
     * message that is useful in Splunk; we can't guarantee that in this
     * class with our own logging because the client may disable our logging.
     */
    protected class RestTemplateErrorHandler extends DefaultResponseErrorHandler {
        @Override
        protected boolean hasError(HttpStatus statusCode) {
            return clientErrorHandler.isErrorCode(statusCode.value());
        }

        @Override
        public void handleError(ClientHttpResponse response) throws IOException {
            throw clientErrorHandler.handleError(REQUEST_CONTEXT.get(), response.getStatusCode().value(), response);
        }
    }
}
