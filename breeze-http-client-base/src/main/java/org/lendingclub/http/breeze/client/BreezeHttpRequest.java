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

package org.lendingclub.http.breeze.client;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.lendingclub.http.breeze.client.exception.BreezeHttpException;
import org.lendingclub.http.breeze.client.filter.BreezeHttpRequestFilter;
import org.lendingclub.http.breeze.client.payload.builder.FormPayloadBuilderRequest;
import org.lendingclub.http.breeze.client.payload.builder.ListPayloadBuilderRequest;
import org.lendingclub.http.breeze.client.payload.builder.MapPayloadBuilderRequest;

/**
 * Encapsulates a REST request, including url/path/query variables, headers,
 * and anything else required. Null values are generally not allowed as REST
 * implementations don't always handle them consistently and result in removing
 * the named value.
 *
 * Filter prepareRequest methods will be invoked in the constructor; actual
 * get/put/post/etc methods will invoke finalizeRequest and then delegate
 * the call to the underlying client.
 *
 * @author Raul Acevedo
 */
public class BreezeHttpRequest {
    public enum Method {
        GET, HEAD, POST, PUT, PATCH, DELETE, OPTIONS, TRACE
    }

    private final BreezeHttpClient client;
    private final List<BreezeHttpRequestFilter> filters = new ArrayList<>();
    private final Map<String, Object> pathVariables = new HashMap<>();
    private final Map<String, List<Object>> queryVariables = new HashMap<>();
    private final Map<String, List<String>> headers = new HashMap<>();
    private final Map<String, String> headerLoggableValues = new HashMap<>();
    private String rootUrl = null;
    private String remoteService = null;
    private String requestName = null;
    private String path = "";
    private Method method = null;

    /** Create a new request for the given client and with its request filters. */
    public BreezeHttpRequest(BreezeHttpClient client) {
        this(null, null, client, client.getRequestFilters());
    }

    /** Create a new request to the given rootUrl and the given client, using the client's request filters. */
    public BreezeHttpRequest(String rootUrl, BreezeHttpClient client) {
        this(rootUrl, null, client, client.getRequestFilters());
    }

    /** Create a new request to the given client and filters. */
    public BreezeHttpRequest(BreezeHttpClient client, List<BreezeHttpRequestFilter> filters) {
        this(null, null, client, filters);
    }

    /** Create a new request to the given rootUrl, client and filters. */
    public BreezeHttpRequest(String rootUrl, BreezeHttpClient client, List<BreezeHttpRequestFilter> filters) {
        this(rootUrl, null, client, filters);
    }

    /** Create a new request for the given client, using the specified filters rather than the client's filters. */
    public BreezeHttpRequest(
            String rootUrl,
            String remoteService,
            BreezeHttpClient client,
            List<BreezeHttpRequestFilter> filters
    ) {
        this.rootUrl = rootUrl;
        this.remoteService = remoteService;
        this.client = client;

        if (filters != null && !filters.isEmpty()) {
            this.filters.addAll(filters);
            this.filters.forEach((filter) -> filter.prepareRequest(this));
        }
    }

    /** Copy constructor; does not invoke filter prepareRequest methods. */
    public BreezeHttpRequest(BreezeHttpRequest request) {
        this.rootUrl = request.rootUrl;
        this.client = request.client;
        this.filters.addAll(request.filters);
        this.pathVariables.putAll(request.pathVariables);
        this.queryVariables.putAll(request.queryVariables);
        this.headers.putAll(request.headers);
        this.headerLoggableValues.putAll(request.headerLoggableValues);
        this.path = request.path;
        this.remoteService = request.remoteService;
        this.requestName = request.requestName;
        this.method = request.method;
    }

    /** Set the rootUrl for this request. */
    public BreezeHttpRequest rootUrl(String rootUrl) {
        this.rootUrl = rootUrl;
        return this;
    }

    /** Optional name for remote service being invoked; used for Graphite metrics and helpful in Splunk searches. */
    public BreezeHttpRequest remoteService(String name) {
        this.remoteService = name;
        return this;
    }

    /** Optional name for request; used for Graphite metrics and helpful in Splunk searches. */
    public BreezeHttpRequest name(String name) {
        this.requestName = name;
        return this;
    }

    /** Set the path. A null value is treated as empty string. */
    public BreezeHttpRequest path(String path) {
        this.path = (path == null ? "" : path);
        return this;
    }

    /** Set a path variable; a null value removes the prior mapping. */
    public BreezeHttpRequest pathVariable(String name, Object value) {
        if (value == null) {
            pathVariables.remove(name);
        } else {
            pathVariables.put(name, value);
        }
        return this;
    }

    /** Set a query string variable; a null value removes the prior mapping. */
    public BreezeHttpRequest queryVariable(String name, Object value) {
        if (value == null) {
            queryVariables.remove(name);
        } else {
            List<Object> values = queryVariables.get(name);
            if (values == null) {
                values = new ArrayList<>();
                queryVariables.put(name, values);
            }
            values.add(value);
        }
        return this;
    }

    /** Add a header; a null value removes the prior mapping. */
    public BreezeHttpRequest header(String name, String value) {
        return header(name, value, null);
    }

    /**
     * Add a header; a null value removes the prior mapping. If shouldLogValue
     * is true, the header value will be included in the toString of this
     * instance, which means it can show up in Splunk logs.
     */
    public BreezeHttpRequest header(String name, String value, boolean shouldLogValue) {
        return header(name, value, shouldLogValue ? value : null);
    }

    /** Add all headers. */
    public BreezeHttpRequest headers(Map<String, String> headers) {
        headers(headers, false);
        return this;
    }

    /** Add all headers, all of them loggable according to shouldLogValue. */
    public BreezeHttpRequest headers(Map<String, String> headers, boolean shouldLogValue) {
        headers.forEach((key, value) -> header(key, value, shouldLogValue));
        return this;
    }

    /**
     * Add a header; a null value removes the prior mapping. If loggableValue is
     * not null, it is the value to use in the toString of this request
     * instance, which means it can show up in Splunk logs. This is useful
     * for encoded headers, e.g. CORRELATION_JSON which is sent UTF-8 encoded
     * but which we'd like to see in Splunk in its raw JSON form.
     */
    public BreezeHttpRequest header(String name, String value, String loggableValue) {
        if (value == null) {
            headers.remove(name);
            headerLoggableValues.remove(name);
            return this;
        }

        List<String> values = headers.get(name);
        if (values == null) {
            values = new ArrayList<>();
            headers.put(name, values);
        }
        values.add(value);

        if (loggableValue != null) {
            headerLoggableValues.put(name, value);
        }

        return this;
    }

    public BreezeHttpRequest method(Method method) {
        this.method = method;
        return this;
    }

    public BreezeHttpRequest method(String method) {
        return method(Method.valueOf(method));
    }

    public FormPayloadBuilderRequest form() {
        return new FormPayloadBuilderRequest(this);
    }

    public MapPayloadBuilderRequest map() {
        return new MapPayloadBuilderRequest(this);
    }

    public ListPayloadBuilderRequest list() {
        return new ListPayloadBuilderRequest(this);
    }

    public String getRemoteService() {
        return remoteService;
    }

    public String getRequestName() {
        return requestName;
    }

    /** Return the remote service name, or "default" if it's not set. */
    public String getDefaultedRemoteService() {
        return remoteService == null ? "default" : remoteService;
    }

    /** Return the request name; if it's null return the requet method instead. */
    public String getDefaultedRequestName() {
        return requestName == null ? method.toString() : requestName;
    }

    public String getRootUrl() {
        return rootUrl;
    }

    public String getPath() {
        return path;
    }

    public Map<String, Object> getPathVariables() {
        return pathVariables;
    }

    public Map<String, List<Object>> getQueryVariables() {
        return queryVariables;
    }

    public Map<String, List<String>> getHeaders() {
        return headers;
    }

    public Method getMethod() {
        return method;
    }

    public BreezeHttpClient getClient() {
        return client;
    }

    public void get() {
        method(Method.GET).finalizeRequest().client.get(this);
    }

    public <T> T get(Class<T> responseType) {
        return method(Method.GET).finalizeRequest().client.get(this, responseType);
    }

    public <T> T get(BreezeHttpType<T> genericType) {
        return method(Method.GET).finalizeRequest().client.get(this, genericType);
    }

    public void post(Object payload) {
        method(Method.POST).finalizeRequest().client.post(this, payload);
    }

    public <T> T post(Class<T> responseType, Object payload) throws BreezeHttpException {
        return method(Method.POST).finalizeRequest().client.post(this, responseType, payload);
    }

    public <T> T post(BreezeHttpType<T> genericType, Object payload) throws BreezeHttpException {
        return method(Method.POST).finalizeRequest().client.post(this, genericType, payload);
    }

    public void put(Object payload) throws BreezeHttpException {
        method(Method.PUT).finalizeRequest().client.put(this, payload);
    }

    public <T> T put(Class<T> responseType, Object payload) throws BreezeHttpException {
        return method(Method.PUT).finalizeRequest().client.put(this, responseType, payload);
    }

    public <T> T put(BreezeHttpType<T> genericType, Object payload) throws BreezeHttpException {
        return method(Method.PUT).finalizeRequest().client.put(this, genericType, payload);
    }

    public void patch(Object payload) throws BreezeHttpException {
        method(Method.PATCH).finalizeRequest().client.patch(this, payload);
    }

    public <T> T patch(Class<T> responseType, Object payload) throws BreezeHttpException {
        return method(Method.PATCH).finalizeRequest().client.patch(this, responseType, payload);
    }

    public <T> T patch(BreezeHttpType<T> genericType, Object payload) throws BreezeHttpException {
        return method(Method.PATCH).finalizeRequest().client.patch(this, genericType, payload);
    }

    public <T> BreezeHttpResponse<T> execute(Class<T> responseClass, Object payload) throws BreezeHttpException {
        return execute(new BreezeHttpType<>(responseClass), payload);
    }

    public <T> BreezeHttpResponse<T> execute(BreezeHttpType<T> responseType, Object payload) throws BreezeHttpException {
        return finalizeRequest().client.execute(this, responseType, payload);
    }

    private BreezeHttpRequest finalizeRequest() {
        filters.forEach((filter) -> filter.finalizeRequest(this));
        return this;
    }

    @Override
    @SuppressWarnings("checkstyle:all")
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        BreezeHttpRequest that = (BreezeHttpRequest) o;

        if (client != null ? !client.equals(that.client) : that.client != null) {
            return false;
        }
        if (filters != null ? !filters.equals(that.filters) : that.filters != null) {
            return false;
        }
        if (pathVariables != null ? !pathVariables.equals(that.pathVariables) : that.pathVariables != null) {
            return false;
        }
        if (queryVariables != null ? !queryVariables.equals(that.queryVariables) : that.queryVariables != null) {
            return false;
        }
        if (headers != null ? !headers.equals(that.headers) : that.headers != null) {
            return false;
        }
        if (headerLoggableValues != null ? !headerLoggableValues.equals(that.headerLoggableValues) : that.headerLoggableValues != null) {
            return false;
        }
        if (rootUrl != null ? !rootUrl.equals(that.rootUrl) : that.rootUrl != null) {
            return false;
        }
        if (path != null ? !path.equals(that.path) : that.path != null) {
            return false;
        }
        if (method != that.method) {
            return false;
        }
        if (remoteService != null ? !remoteService.equals(that.remoteService) : that.remoteService != null) {
            return false;
        }
        return requestName != null ? requestName.equals(that.requestName) : that.requestName == null;
    }

    @Override
    public int hashCode() {
        int result = client != null ? client.hashCode() : 0;
        result = 31 * result + (filters != null ? filters.hashCode() : 0);
        result = 31 * result + (pathVariables != null ? pathVariables.hashCode() : 0);
        result = 31 * result + (queryVariables != null ? queryVariables.hashCode() : 0);
        result = 31 * result + (headers != null ? headers.hashCode() : 0);
        result = 31 * result + (headerLoggableValues != null ? headerLoggableValues.hashCode() : 0);
        result = 31 * result + (rootUrl != null ? rootUrl.hashCode() : 0);
        result = 31 * result + (path != null ? path.hashCode() : 0);
        result = 31 * result + (method != null ? method.hashCode() : 0);
        result = 31 * result + (remoteService != null ? remoteService.hashCode() : 0);
        result = 31 * result + (requestName != null ? requestName.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        StringBuilder b = new StringBuilder(getClass().getSimpleName() + "{"
                + client
                + ", rootUrl=" + rootUrl
                + ", remoteService=" + remoteService
                + ", requestName=" + requestName
                + ", method=" + method
                + ", path=" + path
                + ", queryVariables=" + queryVariables.keySet()
                + ", headers:");

        if (headers.isEmpty()) {
            b.append(" none");
        } else {
            headers.keySet().forEach((header) -> {
                b.append(" ").append(header);
                String loggableValue = headerLoggableValues.get(header);
                if (loggableValue != null) {
                    b.append("=\"").append(loggableValue).append("\"");
                }
            });
        }

        return b.append("}").toString();
    }
}
