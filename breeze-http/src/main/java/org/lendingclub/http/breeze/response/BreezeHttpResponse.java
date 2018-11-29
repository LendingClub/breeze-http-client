package org.lendingclub.http.breeze.response;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import static org.lendingclub.http.breeze.response.BreezeHttpResponse.HttpStatusClass.CLIENT_ERROR;
import static org.lendingclub.http.breeze.response.BreezeHttpResponse.HttpStatusClass.INFORMATIONAL;
import static org.lendingclub.http.breeze.response.BreezeHttpResponse.HttpStatusClass.REDIRECT;
import static org.lendingclub.http.breeze.response.BreezeHttpResponse.HttpStatusClass.SERVER_ERROR;
import static org.lendingclub.http.breeze.response.BreezeHttpResponse.HttpStatusClass.SUCCESS;
import static org.lendingclub.http.breeze.util.BreezeHttpUtil.quote;
import static org.lendingclub.http.breeze.util.BreezeHttpUtil.simpleName;

/**
 * HTTP response class with status code, headers, and body.
 */
public class BreezeHttpResponse<T> {
    public enum HttpStatusClass {
        UNKNOWN,
        INFORMATIONAL,
        SUCCESS,
        REDIRECT,
        CLIENT_ERROR,
        SERVER_ERROR;

        public static HttpStatusClass valueOf(int httpStatus) {
            return httpStatus >= 100 && httpStatus < 600 ? values()[httpStatus / 100] : UNKNOWN;
        }
    }

    protected T body;
    protected int httpStatus;
    protected final Map<String, List<String>> headers = new TreeMap<>();

    public BreezeHttpResponse() {
        this(null, 0);
    }

    public BreezeHttpResponse(T body) {
        this(body, 0);
    }

    public BreezeHttpResponse(T body, int httpStatus) {
        this(body, httpStatus, null);
    }

    public BreezeHttpResponse(T body, int httpStatus, Map<String, List<String>> headers) {
        this.body = body;
        this.httpStatus = httpStatus;
        if (headers != null) {
            headers.forEach((key, value) -> this.headers.put(key, new ArrayList<>(value)));
        }
    }

    public BreezeHttpResponse(T body, BreezeHttpRawResponse raw) {
        this(body, raw.httpStatus, raw.headers);
    }

    public T body() {
        return body;
    }

    public void body(T body) {
        this.body = body;
    }

    public int httpStatus() {
        return httpStatus;
    }

    public void httpStatus(int httpStatus) {
        this.httpStatus = httpStatus;
    }

    public HttpStatusClass httpStatusClass() {
        return HttpStatusClass.valueOf(httpStatus);
    }

    public boolean isInformational() {
        return httpStatusClass() == INFORMATIONAL;
    }

    public boolean isSuccess() {
        return httpStatusClass() == SUCCESS;
    }

    public boolean isRedirect() {
        return httpStatusClass() == REDIRECT;
    }

    public boolean isClientError() {
        return httpStatusClass() == CLIENT_ERROR;
    }

    public boolean isServerError() {
        return httpStatusClass() == SERVER_ERROR;
    }

    public boolean isError() {
        return isClientError() || isServerError();
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
        List<String> values = headers(name);
        return values == null ? null : values.get(0);
    }

    public void header(String name, List<String> values) {
        headers.put(name, new ArrayList<>(values));
    }

    public void headers(Map<String, List<String>> headers) {
        this.headers.putAll(headers);
    }

    public String contentType() {
        return header("Content-Type");
    }

    @Override
    public String toString() {
        return "BreezeHttpResponse@" + Integer.toHexString(hashCode()) + "{"
                + "body=" + simpleName(body)
                + " httpStatus=" + httpStatus
                + " headers=" + quote(headers.keySet())
                + "}";
    }
}
