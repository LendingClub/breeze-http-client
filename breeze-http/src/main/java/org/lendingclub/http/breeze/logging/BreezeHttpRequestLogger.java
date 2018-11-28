package org.lendingclub.http.breeze.logging;

import org.lendingclub.http.breeze.request.BreezeHttpRequest;
import org.lendingclub.http.breeze.response.BreezeHttpResponse;

public interface BreezeHttpRequestLogger {
    void start(BreezeHttpRequest request);

    void end(BreezeHttpRequest request, BreezeHttpResponse<?> response);

    void exception(BreezeHttpRequest request, Throwable t);
}
