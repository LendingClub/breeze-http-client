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

public class BreezeHttpResponseException extends BreezeHttpException {
    public static final long serialVersionUID = -1;

    private final BreezeHttpRequest request;
    private final BreezeHttpResponse<?> response;

    public BreezeHttpResponseException(BreezeHttpRequest request, BreezeHttpResponse<?> response) {
        this("HTTP error response executing request", request, response, null);
    }

    public BreezeHttpResponseException(String message, BreezeHttpRequest request, BreezeHttpResponse<?> response) {
        this(message, request, response, null);
    }

    public BreezeHttpResponseException(BreezeHttpRequest request, BreezeHttpResponse<?> response, Throwable t) {
        this("HTTP error response executing request", request, response, t);
    }

    public BreezeHttpResponseException(
            String message,
            BreezeHttpRequest request,
            BreezeHttpResponse<?> response,
            Throwable t
    ) {
        super(message, t);
        this.request = request;
        this.response = response;
    }

    public BreezeHttpRequest request() {
        return request;
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

    public static BreezeHttpResponseException create(BreezeHttpRequest request, BreezeHttpResponse<?> response) {
        if (response != null) {
            if (response.httpStatusClass() == CLIENT_ERROR) {
                return new BreezeHttpClientErrorException(request, response);
            } else if (response.httpStatusClass() == SERVER_ERROR) {
                return new BreezeHttpServerErrorException(request, response);
            }
        }
        return new BreezeHttpResponseException(request, response);
    }

    public static BreezeHttpResponseException create(
            BreezeHttpRequest request,
            BreezeHttpResponse<?> response,
            Throwable t
    ) {
        if (response != null) {
            if (response.httpStatusClass() == CLIENT_ERROR) {
                return new BreezeHttpClientErrorException(request, response, t);
            } else if (response.httpStatusClass() == SERVER_ERROR) {
                return new BreezeHttpServerErrorException(request, response, t);
            }
        }
        return new BreezeHttpResponseException(request, response, t);
    }
}
