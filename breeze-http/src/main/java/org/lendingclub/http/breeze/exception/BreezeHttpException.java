package org.lendingclub.http.breeze.exception;

import org.lendingclub.http.breeze.request.BreezeHttpRequest;
import org.lendingclub.http.breeze.response.BreezeHttpRawResponse;
import org.lendingclub.http.breeze.response.BreezeHttpResponse;

import java.io.IOException;

import static org.lendingclub.http.breeze.response.BreezeHttpResponse.HttpStatusClass.CLIENT_ERROR;
import static org.lendingclub.http.breeze.response.BreezeHttpResponse.HttpStatusClass.SERVER_ERROR;

public class BreezeHttpException extends RuntimeException {
    public static final long serialVersionUID = -1;

    public BreezeHttpException(String message) {
        super(message);
    }

    public BreezeHttpException(String message, Throwable cause) {
        super(message, cause);
    }

    public BreezeHttpException(Throwable cause) {
        super(cause);
    }

    public IOException findIOException() {
        return findIOException(this);
    }

    public static IOException findIOException(Throwable t) {
        while (t != null) {
            if (t instanceof IOException) {
                return (IOException) t;
            }
            t = t.getCause();
        }
        return null;
    }

    public static BreezeHttpExecutionException create(
            BreezeHttpRequest request,
            BreezeHttpRawResponse raw,
            BreezeHttpResponse<?> response,
            Throwable cause
    ) {
        if (cause instanceof BreezeHttpExecutionException) {
            return (BreezeHttpExecutionException) cause;
        }

        if (response == null) {
            response = raw;
        }

        if (response == null) {
            return new BreezeHttpExecutionException(request, cause);
        } else if (response.httpStatusClass() == CLIENT_ERROR) {
            return new BreezeHttpClientErrorException(request, response, cause);
        } else if (response.httpStatusClass() == SERVER_ERROR) {
            return new BreezeHttpServerErrorException(request, response, cause);
        } else {
            return new BreezeHttpResponseException(request, response, cause);
        }
    }
}
