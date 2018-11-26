package org.lendingclub.http.breeze.exception;

import org.lendingclub.http.breeze.request.BreezeHttpRequest;
import org.lendingclub.http.breeze.response.BreezeHttpResponse;

public class BreezeHttpClientErrorException extends BreezeHttpResponseException {
    public static final long serialVersionUID = -1;

    public BreezeHttpClientErrorException(BreezeHttpRequest request, BreezeHttpResponse<?> response) {
        super(request, response);
    }

    public BreezeHttpClientErrorException(String message, BreezeHttpRequest request, BreezeHttpResponse<?> response) {
        super(message, request, response);
    }

    public BreezeHttpClientErrorException(BreezeHttpRequest request, BreezeHttpResponse<?> response, Throwable t) {
        super(request, response, t);
    }

    public BreezeHttpClientErrorException(
            String message,
            BreezeHttpRequest request,
            BreezeHttpResponse<?> response,
            Throwable t
    ) {
        super(message, request, response, t);
    }
}