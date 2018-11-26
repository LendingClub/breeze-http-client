package org.lendingclub.http.breeze.exception;

import org.lendingclub.http.breeze.request.BreezeHttpRequest;
import org.lendingclub.http.breeze.response.BreezeHttpResponse;

public class BreezeHttpServerErrorException extends BreezeHttpResponseException {
    public static final long serialVersionUID = -1;

    public BreezeHttpServerErrorException(BreezeHttpRequest request, BreezeHttpResponse<?> response) {
        super(request, response);
    }

    public BreezeHttpServerErrorException(String message, BreezeHttpRequest request, BreezeHttpResponse<?> response) {
        super(message, request, response);
    }

    public BreezeHttpServerErrorException(BreezeHttpRequest request, BreezeHttpResponse<?> response, Throwable t) {
        super(request, response, t);
    }

    public BreezeHttpServerErrorException(
            String message,
            BreezeHttpRequest request,
            BreezeHttpResponse<?> response,
            Throwable t
    ) {
        super(message, request, response, t);
    }
}
