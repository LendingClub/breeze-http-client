package org.lendingclub.http.breeze.error;

import org.lendingclub.http.breeze.exception.BreezeHttpExecutionException;
import org.lendingclub.http.breeze.request.BreezeHttpRequest;
import org.lendingclub.http.breeze.response.BreezeHttpRawResponse;
import org.lendingclub.http.breeze.response.BreezeHttpResponse;

public interface BreezeHttpErrorHandler {
    /** If true, handleError is called. */
    default boolean isError(BreezeHttpRequest request, BreezeHttpRawResponse raw) {
        return false;
    }

    /** Invoked when isError returns true. */
    default <T> BreezeHttpResponse<T> handleError(BreezeHttpRequest request, BreezeHttpRawResponse raw) {
        return null;
    }

    /** Invoked when an exception is thrown; response may be raw. */
    default <T> BreezeHttpResponse<T> handleException(BreezeHttpExecutionException e) {
        return null;
    }
}
