package org.lendingclub.http.breeze.exception;

import org.lendingclub.http.breeze.request.BreezeHttpRequest;

public class BreezeHttpExecutionException extends BreezeHttpException {
    public static final long serialVersionUID = -1;

    private final BreezeHttpRequest request;

    public BreezeHttpExecutionException(String message, BreezeHttpRequest request, Throwable cause) {
        super(message, cause);
        this.request = request;
    }

    public BreezeHttpExecutionException(BreezeHttpRequest request, Throwable cause) {
        super("error executing request", cause);
        this.request = request;
    }

    public BreezeHttpRequest request() {
        return request;
    }
}
