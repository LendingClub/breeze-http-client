package org.lendingclub.http.breeze.exception;

import org.lendingclub.http.breeze.request.BreezeHttpRequest;
import org.lendingclub.http.breeze.response.BreezeHttpRawResponse;
import org.lendingclub.http.breeze.response.BreezeHttpResponse;

public class BreezeHttpClientErrorException extends BreezeHttpResponseException {
    public static final long serialVersionUID = -1;

    public BreezeHttpClientErrorException(
            BreezeHttpRequest request,
            BreezeHttpRawResponse raw,
            BreezeHttpResponse<?> response,
            Throwable t
    ) {
        super(request, raw, response, t);
    }
}
