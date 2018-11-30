package org.lendingclub.http.breeze.request;

import org.lendingclub.http.breeze.BreezeHttp;
import org.lendingclub.http.breeze.decorator.DecoratedBreezeHttp;
import org.lendingclub.http.breeze.exception.BreezeHttpException;
import org.lendingclub.http.breeze.filter.BreezeHttpFilter;
import org.lendingclub.http.breeze.request.body.builder.FormRequestBodyBuilder;
import org.lendingclub.http.breeze.request.body.builder.ListRequestBodyBuilder;
import org.lendingclub.http.breeze.request.body.builder.MapRequestBodyBuilder;
import org.lendingclub.http.breeze.request.body.builder.MultipartRequestBodyBuilder;
import org.lendingclub.http.breeze.response.BreezeHttpRawResponse;
import org.lendingclub.http.breeze.response.BreezeHttpResponse;
import org.lendingclub.http.breeze.type.BreezeHttpType;

import java.lang.reflect.Type;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import static org.lendingclub.http.breeze.type.BreezeHttpType.firstTypeArgument;
import static org.lendingclub.http.breeze.type.BreezeHttpType.isSubclass;
import static org.lendingclub.http.breeze.type.BreezeHttpType.rawType;
import static org.lendingclub.http.breeze.util.BreezeHttpUtil.decode;
import static org.lendingclub.http.breeze.util.BreezeHttpUtil.quote;

/**
 * Encapsulates an HTTP request, including url/path/query variables, headers,
 * and anything else required. Null values are generally not allowed as REST
 * implementations don't always handle them consistently and result in removing
 * the named value.
 *
 * Filter init methods will be invoked in the constructor.
 */
public class BreezeHttpRequest {
    public enum Method {
        GET, HEAD, POST, PUT, PATCH, DELETE, OPTIONS, TRACE
    }

    private final BreezeHttp breeze;
    private final Map<String, Object> pathVariables = new HashMap<>();
    private final Map<String, List<Object>> queryParameters = new TreeMap<>();
    private final Map<String, List<String>> headers = new TreeMap<>();
    private final Map<String, String> headerLoggableValues = new HashMap<>();
    private String url = null;
    private String service = null;
    private String name = null;
    private String path = "";
    private Method method = null;
    private Object body = null;
    private Type returnType = null;
    private long duration = -1;
    private boolean bufferResponse = false;

    public BreezeHttpRequest() {
        this.breeze = null;
    }

    /** Create a new request for the given client and with its request filters. */
    public BreezeHttpRequest(BreezeHttp breeze) {
        this("", null, breeze, breeze.filters());
    }

    /** Create a new request to the given rootUrl and the given client, using the client's request filters. */
    public BreezeHttpRequest(URL url, BreezeHttp breeze) {
        this(url, null, breeze, breeze.filters());
    }

    /** Create a new request to the given rootUrl and the given client, using the client's request filters. */
    public BreezeHttpRequest(String url, BreezeHttp breeze) {
        this(toURL(url), null, breeze, breeze.filters());
    }

    /** Create a new request to the given client and filters. */
    public BreezeHttpRequest(BreezeHttp breeze, List<BreezeHttpFilter> filters) {
        this("", null, breeze, filters);
    }

    /** Create a new request to the given rootUrl, client and filters. */
    public BreezeHttpRequest(String url, BreezeHttp breeze, List<BreezeHttpFilter> filters) {
        this(toURL(url), null, breeze, filters);
    }

    public BreezeHttpRequest(String url, String service, BreezeHttp breeze, List<BreezeHttpFilter> filters) {
        this(toURL(url), service, breeze, filters);
    }

    /** Create a new request for the given client, using the specified filters rather than the client's filters. */
    public BreezeHttpRequest(URL url, String service, BreezeHttp breeze, List<BreezeHttpFilter> filters) {
        this.service = service;
        this.breeze = breeze;
        url(url);
        breezeUserAgent();

        if (filters != null && !filters.isEmpty()) {
            this.breeze.filters().addAll(filters);
            BreezeHttpFilter.filter(this, filter -> filter.created(this));
        }
    }

    /** Copy constructor; does not invoke filter init methods. */
    public BreezeHttpRequest(BreezeHttpRequest request) {
        this(request, request.breeze);
    }

    /** Copy constructor; does not invoke filter init methods. */
    public BreezeHttpRequest(BreezeHttpRequest request, BreezeHttp breeze) {
        this.breeze = breeze;
        this.url = request.url;
        this.pathVariables.putAll(request.pathVariables);
        this.queryParameters.putAll(request.queryParameters);
        this.headers.putAll(request.headers);
        this.headerLoggableValues.putAll(request.headerLoggableValues);
        this.path = request.path;
        this.service = request.service;
        this.name = request.name;
        this.method = request.method;
        this.body = request.body;
        this.returnType = request.returnType;
        this.bufferResponse = request.bufferResponse;
        this.duration = request.duration;
    }

    private void breezeUserAgent() {
        String version = breeze.getClass().getPackage().getImplementationVersion(); // returns null inside intellij
        BreezeHttp client = breeze instanceof DecoratedBreezeHttp ? ((DecoratedBreezeHttp) breeze).client() : breeze;
        header("User-Agent", "BreezeHttp/" + version + " (" + client.getClass().getSimpleName() + ")", true);
    }

    public BreezeHttp breeze() {
        return breeze;
    }

    public String url() {
        return url;
    }

    /** Set the url for this request. */
    public BreezeHttpRequest url(String url) {
        url(toURL(url));
        return this;
    }

    public BreezeHttpRequest url(URL url) {
        if (url == null) {
            this.url = null;
            return this;
        }

        if (url.getQuery() == null || url.getQuery().length() == 0) {
            this.url = url.toString();
            return this;
        }

        String s = url.toString();
        String query = url.getQuery();
        this.url = s.substring(0, s.length() - query.length() - 1);
        for (String pair : query.split("&")) {
            String[] keyValue = pair.split("=");
            queryParameter(decode(keyValue[0]), keyValue.length == 1 ? "" : decode(keyValue[1]));
        }
        return this;
    }

    private static URL toURL(String url) {
        try {
            return url == null || url.length() == 0 ? null : new URL(url);
        } catch (MalformedURLException e) {
            throw new BreezeHttpException(e);
        }
    }

    /** Optional name for remote service being invoked; used for Graphite metrics and helpful in Splunk searches. */
    public BreezeHttpRequest service(String name) {
        this.service = name;
        return this;
    }

    public String service() {
        return service;
    }

    public String name() {
        return name;
    }

    /** Optional name for request; used for Graphite metrics and helpful in Splunk searches. */
    public BreezeHttpRequest name(String name) {
        this.name = name;
        return this;
    }

    public String path() {
        return path;
    }

    /** Set the path. A null value is treated as empty string. */
    public BreezeHttpRequest path(String path) {
        this.path = path == null ? "" : path;
        return this;
    }

    public Object pathVariable(String name) {
        return pathVariables.get(name);
    }

    public Map<String, Object> pathVariables() {
        return pathVariables;
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

    /** Sets path variables; replaces the current mappings. */
    public BreezeHttpRequest pathVariables(Map<String, Object> pathVariables) {
        this.pathVariables.clear();
        if (pathVariables != null) {
            this.pathVariables.putAll(pathVariables);
        }
        return this;
    }

    public Map<String, List<Object>> queryParameters() {
        return queryParameters;
    }

    public List<Object> queryParameters(String name) {
        return queryParameters.get(name);
    }

    public String queryParameter(String name) {
        List<String> parameters = headers(name);
        return parameters == null ? null : parameters.get(0);
    }

    /** Set a query string variable; a null value removes the prior mapping. */
    public BreezeHttpRequest queryParameter(String name, Object value) {
        if (value == null) {
            queryParameters.remove(name);
        } else {
            queryParameters.computeIfAbsent(name, key -> new ArrayList<>()).add(value);
        }
        return this;
    }

    public BreezeHttpRequest queryParameters(Map<String, List<Object>> queryParameters) {
        this.queryParameters.clear();
        if (queryParameters != null) {
            this.queryParameters.putAll(queryParameters);
        }
        return this;
    }

    public Map<String, List<String>> headers() {
        return headers;
    }

    public List<String> headers(String name) {
        return headers.entrySet().stream()
                .filter(entry -> entry.getKey().equalsIgnoreCase(name))
                .map(Map.Entry::getValue)
                .findFirst()
                .orElse(null);
    }

    public String header(String name) {
        List<String> headers = headers(name);
        return headers == null ? null : headers.get(0);
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

    public String headerLoggableValue(String header) {
        return headerLoggableValues.get(header.toLowerCase());
    }

    /**
     * Add a header; a null value removes the prior mapping. If loggableValue is
     * not null, it is the value to use in the toString of this request
     * instance, which means it can show up in Splunk logs. This is useful
     * for headers sent over the wire in an encoded form but we'd like to log
     * in a more readable string.
     */
    public BreezeHttpRequest header(String name, String value, String loggableValue) {
        if (value == null) {
            headers.remove(name);
            headerLoggableValues.remove(name.toLowerCase());
            return this;
        }

        headers.computeIfAbsent(name, key -> new ArrayList<>()).add(value);
        if (loggableValue != null) {
            headerLoggableValues.put(name.toLowerCase(), value);
        }

        return this;
    }

    public Method method() {
        return method;
    }

    public BreezeHttpRequest method(Method method) {
        this.method = method;
        return this;
    }

    public BreezeHttpRequest method(String method) {
        return method(Method.valueOf(method));
    }

    public Object body() {
        return body;
    }

    public BreezeHttpRequest body(Object body) {
        this.body = body;
        return this;
    }

    public Type returnType() {
        return returnType;
    }

    public BreezeHttpRequest returnType(Class<?> returnClass) {
        this.returnType = returnClass;
        return this;
    }

    public BreezeHttpRequest returnType(Type returnType) {
        this.returnType = returnType;
        return this;
    }

    public <T> BreezeHttpRequest returnType(BreezeHttpType<T> returnType) {
        this.returnType = returnType == null ? null : returnType.type();
        return this;
    }

    public Type conversionType() {
        if (returnType == null || returnType == Void.class || isSubclass(BreezeHttpRawResponse.class, returnType)) {
            return null;
        } else if (rawType(returnType) == BreezeHttpResponse.class) {
            Type conversionType = firstTypeArgument(returnType);
            return conversionType == Void.class ? null : conversionType;
        } else {
            return returnType;
        }
    }

    public long duration() {
        return duration;
    }

    public void duration(long duration) {
        this.duration = duration;
    }

    public boolean bufferResponse() {
        return bufferResponse;
    }

    public BreezeHttpRequest bufferResponse(boolean bufferResponse) {
        this.bufferResponse = bufferResponse;
        return this;
    }

    public String contentType() {
        return header("Content-Type");
    }

    public BreezeHttpRequest contentType(String contentType) {
        return header("Content-Type", contentType, true);
    }

    public BreezeHttpRequest json() {
        return contentType("application/json; charset=utf-8");
    }

    public BreezeHttpRequest gzip() {
        return header("Accept-Encoding", "gzip", true);
    }

    public FormRequestBodyBuilder form() {
        return new FormRequestBodyBuilder(contentType("application/x-www-form-urlencoded"));
    }

    public MultipartRequestBodyBuilder multipart() {
        if (contentType() == null) {
            contentType("multipart/form-data");
        }
        return new MultipartRequestBodyBuilder(this);
    }

    public MultipartRequestBodyBuilder multipart(String contentType) {
        return new MultipartRequestBodyBuilder(contentType(contentType));
    }

    public MapRequestBodyBuilder map() {
        return new MapRequestBodyBuilder(this);
    }

    public ListRequestBodyBuilder list() {
        return new ListRequestBodyBuilder(this);
    }

    public String get() {
        return method(Method.GET).breeze.get(this);
    }

    public <T> T get(Class<T> returnClass) {
        return method(Method.GET).breeze.get(this, returnClass);
    }

    public <T> T get(BreezeHttpType<T> returnType) {
        return method(Method.GET).breeze.get(this, returnType);
    }

    public String post(Object body) {
        return method(Method.POST).breeze.post(this, body);
    }

    public <T> T post(Object body, Class<T> returnClass) throws BreezeHttpException {
        return method(Method.POST).breeze.post(this, body, returnClass);
    }

    public <T> T post(Object body, BreezeHttpType<T> returnType) throws BreezeHttpException {
        return method(Method.POST).breeze.post(this, body, returnType);
    }

    public String put(Object body) throws BreezeHttpException {
        return method(Method.PUT).breeze.put(this, body);
    }

    public <T> T put(Object body, Class<T> returnClass) throws BreezeHttpException {
        return method(Method.PUT).breeze.put(this, body, returnClass);
    }

    public <T> T put(Object body, BreezeHttpType<T> genericType) throws BreezeHttpException {
        return method(Method.PUT).breeze.put(this, body, genericType);
    }

    public String patch(Object body) throws BreezeHttpException {
        return method(Method.PATCH).breeze.patch(this, body);
    }

    public <T> T patch(Object body, Class<T> returnClass) throws BreezeHttpException {
        return method(Method.PATCH).breeze.patch(this, body, returnClass);
    }

    public <T> T patch(Object body, BreezeHttpType<T> returnType) throws BreezeHttpException {
        return method(Method.PATCH).breeze.patch(this, body, returnType);
    }

    public String delete() {
        return method(Method.DELETE).breeze.get(this);
    }

    public String delete(Object body) throws BreezeHttpException {
        return method(Method.DELETE).breeze.put(this, body);
    }

    public <T> T delete(Object body, Class<T> returnClass) throws BreezeHttpException {
        return method(Method.DELETE).breeze.put(this, body, returnClass);
    }

    public <T> T delete(Object body, BreezeHttpType<T> returnType) throws BreezeHttpException {
        return method(Method.DELETE).breeze.put(this, body, returnType);
    }

    public Map<String, List<String>> head(String url) throws BreezeHttpException {
        return url(url).method(Method.HEAD).breeze.head(this);
    }

    public Map<String, List<String>> head() throws BreezeHttpException {
        return method(Method.HEAD).breeze.head(this);
    }

    public <T> T execute(Method method, Object body, Class<T> returnClass) throws BreezeHttpException {
        return method(method).execute(body, new BreezeHttpType<>(returnClass));
    }

    public <T> T execute(Object body, Class<T> returnClass) throws BreezeHttpException {
        return execute(body, new BreezeHttpType<>(returnClass));
    }

    public <T> T execute(Object body, BreezeHttpType<T> returnType) throws BreezeHttpException {
        return breeze.execute(this, body, returnType);
    }

    public <T> T execute(Method method, Object body, BreezeHttpType<T> returnType) throws BreezeHttpException {
        return method(method).breeze.execute(this, body, returnType);
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

        if (breeze != null ? !breeze.equals(that.breeze) : that.breeze != null) {
            return false;
        }
        if (pathVariables != null ? !pathVariables.equals(that.pathVariables) : that.pathVariables != null) {
            return false;
        }
        if (queryParameters != null ? !queryParameters.equals(that.queryParameters) : that.queryParameters != null) {
            return false;
        }
        if (headers != null ? !headers.equals(that.headers) : that.headers != null) {
            return false;
        }
        if (headerLoggableValues != null ? !headerLoggableValues.equals(that.headerLoggableValues) : that.headerLoggableValues != null) {
            return false;
        }
        if (url != null ? !url.equals(that.url) : that.url != null) {
            return false;
        }
        if (path != null ? !path.equals(that.path) : that.path != null) {
            return false;
        }
        if (method != that.method) {
            return false;
        }
        if (service != null ? !service.equals(that.service) : that.service != null) {
            return false;
        }
        return name != null ? name.equals(that.name) : that.name == null;
    }

    @Override
    public int hashCode() {
        int result = breeze != null ? breeze.hashCode() : 0;
        result = 31 * result + (pathVariables != null ? pathVariables.hashCode() : 0);
        result = 31 * result + (queryParameters != null ? queryParameters.hashCode() : 0);
        result = 31 * result + (headers != null ? headers.hashCode() : 0);
        result = 31 * result + (headerLoggableValues != null ? headerLoggableValues.hashCode() : 0);
        result = 31 * result + (url != null ? url.hashCode() : 0);
        result = 31 * result + (path != null ? path.hashCode() : 0);
        result = 31 * result + (method != null ? method.hashCode() : 0);
        result = 31 * result + (service != null ? service.hashCode() : 0);
        result = 31 * result + (name != null ? name.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        StringBuilder b = new StringBuilder(toStringShort() + "{"
                + breeze
                + " url=" + quote(url)
                + " service=" + quote(service)
                + " name=" + quote(name)
                + " method=" + quote(method)
                + " path=" + quote(path)
                + " pathVariables=" + quote(pathVariables.keySet())
                + " queryParameters=" + quote(queryParameters.keySet())
                + ", headers:");

        if (headers.isEmpty()) {
            b.append(" none");
        } else {
            headers.keySet().forEach(header -> {
                b.append(" ").append(header);
                String value = headerLoggableValues.get(header.toLowerCase());
                if (value != null) {
                    b.append("=\"").append(value).append("\"");
                }
            });
        }

        return b.append("}").toString();
    }

    public String toStringShort() {
        return getClass().getSimpleName() + "@" + Integer.toHexString(hashCode());
    }
}
