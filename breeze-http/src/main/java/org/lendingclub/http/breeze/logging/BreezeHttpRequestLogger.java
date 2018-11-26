package org.lendingclub.http.breeze.logging;

import org.lendingclub.http.breeze.request.BreezeHttpRequest;
import org.lendingclub.http.breeze.response.BreezeHttpResponse;

public interface BreezeHttpRequestLogger {
    void requestStart(BreezeHttpRequest request);

    void requestEnd(BreezeHttpRequest request, BreezeHttpResponse<?> response);

    void requestError(BreezeHttpRequest request, Throwable t);
}
