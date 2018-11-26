package org.lendingclub.http.breeze.filter;

import org.lendingclub.http.breeze.request.BreezeHttpRequest;
import org.lendingclub.http.breeze.response.BreezeHttpRawResponse;
import org.lendingclub.http.breeze.response.BreezeHttpResponse;

import java.util.Collection;
import java.util.function.Predicate;

/**
 * Filter that is executed when a request is initiated, completes
 * successfully, or receives an error. Filters return true if subsequent
 * filters should be executed; false means don't process any more filters.
 */
public interface BreezeHttpFilter {
    /** Whether any filters methods should be invoked for this request. */
    default boolean shouldFilter(BreezeHttpRequest request) {
        return true;
    }

    /** Called when a request is first created; useful to set defaults. */
    default boolean init(BreezeHttpRequest request) {
        return true;
    }

    /** Called when a request is about to be executed. */
    default boolean prepare(BreezeHttpRequest request) {
        return true;
    }

    /** Called when a request has just been executed, but has not been transformed into a full response. */
    default boolean executed(BreezeHttpRequest request, BreezeHttpRawResponse raw) {
        return true;
    }

    /** Called after the error handler determines the response is an error, but before it is handled. */
    default boolean error(BreezeHttpRequest request, BreezeHttpRawResponse raw) {
        return true;
    }

    /** Called when a request successfully completes; invoked before any other response filters. */
    default boolean complete(BreezeHttpRequest request, BreezeHttpResponse<?> response) {
        return true;
    }

    /** Called when a request throws an exception. */
    default boolean exception(BreezeHttpRequest request, Throwable t) {
        return true;
    }

    static void invoke(
            Collection<BreezeHttpFilter> filters,
            BreezeHttpRequest request,
            Predicate<BreezeHttpFilter> predicate
    ) {
        for (BreezeHttpFilter filter : filters) {
            if (filter.shouldFilter(request) && !predicate.test(filter)) {
                break;
            }
        }
    }
}
