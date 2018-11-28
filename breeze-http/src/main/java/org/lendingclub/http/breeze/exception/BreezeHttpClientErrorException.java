package org.lendingclub.http.breeze.exception;

import org.lendingclub.http.breeze.request.BreezeHttpRequest;
import org.lendingclub.http.breeze.response.BreezeHttpResponse;

public class BreezeHttpClientErrorException extends BreezeHttpResponseException {
    public static final long serialVersionUID = -1;

    public BreezeHttpClientErrorException(BreezeHttpRequest request, BreezeHttpResponse<?> response, Throwable t) {
        super(request, response, t);
    }
}
