package org.lendingclub.http.breeze.exception;

import org.lendingclub.http.breeze.request.BreezeHttpRequest;
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

    private final BreezeHttpResponse<?> response;

    public BreezeHttpResponseException(BreezeHttpRequest request, BreezeHttpResponse<?> response, Throwable t) {
        this("HTTP error response executing request", request, response, t);
    }

    public BreezeHttpResponseException(
            String message,
            BreezeHttpRequest request,
            BreezeHttpResponse<?> response,
            Throwable t
    ) {
        super(message, request, t);
        this.response = response;
    }

    public <T> BreezeHttpResponse<T> response() {
        return cast(response);
    }

    public <T> T body() {
        return response == null ? null : cast(response.body());
    }

    public int httpStatus() {
        return response == null ? 0 : response.httpStatus();
    }

    public HttpStatusClass httpStatusClass() {
        return response == null ? null : response.httpStatusClass();
    }

    public boolean isClientError() {
        return httpStatusClass() == CLIENT_ERROR;
    }

    public boolean isServerError() {
        return httpStatusClass() == SERVER_ERROR;
    }

    public Map<String, List<String>> headers() {
        return response == null ? emptyMap() : response.headers();
    }

    public List<String> headers(String name) {
        return response == null ? emptyList() : response.headers(name);
    }

    public String header(String name) {
        return response == null ? null : response.header(name);
    }
}
