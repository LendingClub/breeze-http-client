package org.lendingclub.http.breeze.exception;

import org.lendingclub.http.breeze.request.BreezeHttpRequest;
import org.lendingclub.http.breeze.response.BreezeHttpRawResponse;
import org.lendingclub.http.breeze.response.BreezeHttpResponse;

public class BreezeHttpExecutionException extends BreezeHttpException {
    public static final long serialVersionUID = -1;

    protected final BreezeHttpRequest request;
    protected final BreezeHttpRawResponse raw;
    protected final BreezeHttpResponse<?> response;

    public BreezeHttpExecutionException(
            BreezeHttpRequest request,
            BreezeHttpRawResponse raw,
            BreezeHttpResponse<?> response,
            Throwable cause) {
        super(cause);
        this.request = request;
        this.raw = raw;
        this.response = response;
    }

    public BreezeHttpRequest request() {
        return request;
    }

    public BreezeHttpRawResponse raw() {
        return raw;
    }

    public BreezeHttpResponse<?> response() {
        return response;
    }

    public static BreezeHttpExecutionException create(
            BreezeHttpRequest request,
            BreezeHttpRawResponse raw,
            BreezeHttpResponse<?> response,
            Throwable t
    ) {
        if (t instanceof BreezeHttpExecutionException) {
            return (BreezeHttpExecutionException) t;
        }

        if (raw == null) {
            return new BreezeHttpExecutionException(request, null, null, t);
        } else if (raw.isClientError()) {
            return new BreezeHttpClientErrorException(request, raw, response, t);
        } else if (raw.isServerError()) {
            return new BreezeHttpServerErrorException(request, raw, response, t);
        } else {
            return new BreezeHttpResponseException(request, raw, response, t);
        }
    }
}
