package org.lendingclub.http.breeze.converter;

import org.lendingclub.http.breeze.request.BreezeHttpRequest;
import org.lendingclub.http.breeze.response.BreezeHttpRawResponse;
import org.lendingclub.http.breeze.response.BreezeHttpResponse;

import java.lang.reflect.Type;

public interface BreezeHttpConverter {
    boolean convertRequestBody(BreezeHttpRequest request);

    <T> BreezeHttpResponse<T> convertResponse(BreezeHttpRawResponse raw, Type type);
}
