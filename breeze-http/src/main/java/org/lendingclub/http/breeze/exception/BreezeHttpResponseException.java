package org.lendingclub.http.breeze.exception;

import org.lendingclub.http.breeze.request.BreezeHttpRequest;
import org.lendingclub.http.breeze.response.BreezeHttpRawResponse;
import org.lendingclub.http.breeze.response.BreezeHttpResponse;
import org.lendingclub.http.breeze.response.BreezeHttpResponse.HttpStatusClass;

import java.util.List;
import java.util.Map;

import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static org.lendingclub.http.breeze.response.BreezeHttpResponse.HttpStatusClass.CLIENT_ERROR;
import static org.lendingclub.http.breeze.response.BreezeHttpResponse.HttpStatusClass.SERVER_ERROR;
import static org.lendingclub.http.breeze.util.BreezeHttpUtil.cast;

public class BreezeHttpResponseException extends BreezeHttpExecutionException {
    public static final long serialVersionUID = -1;

    public BreezeHttpResponseException(
            BreezeHttpRequest request,
            BreezeHttpRawResponse raw,
            BreezeHttpResponse<?> response,
            Throwable t
    ) {
        super(request, raw, response, t);
    }

    public <T> T body() {
        return response == null ? null : cast(response.body());
    }

    public int httpStatus() {
        return raw == null ? 0 : raw.httpStatus();
    }

    public HttpStatusClass httpStatusClass() {
        return raw == null ? null : raw.httpStatusClass();
    }

    public boolean isClientError() {
        return httpStatusClass() == CLIENT_ERROR;
    }

    public boolean isServerError() {
        return httpStatusClass() == SERVER_ERROR;
    }

    public Map<String, List<String>> headers() {
        return raw == null ? emptyMap() : raw.headers();
    }

    public List<String> headers(String name) {
        return raw == null ? emptyList() : raw.headers(name);
    }

    public String header(String name) {
        return raw == null ? null : raw.header(name);
    }
}
