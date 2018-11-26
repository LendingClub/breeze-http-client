package org.lendingclub.http.breeze.error;

import org.lendingclub.http.breeze.request.BreezeHttpRequest;
import org.lendingclub.http.breeze.response.BreezeHttpRawResponse;

public interface BreezeHttpErrorHandler {
    default boolean isError(BreezeHttpRequest request, BreezeHttpRawResponse raw) {
        return false;
    }

    /** Return new response or throw custom exception that has parsed the raw response. */
    default void handleError(BreezeHttpRequest request, BreezeHttpRawResponse raw) {
    }
}
