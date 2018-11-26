package org.lendingclub.http.breeze.converter;

import org.lendingclub.http.breeze.request.BreezeHttpRequest;
import org.lendingclub.http.breeze.response.BreezeHttpRawResponse;
import org.lendingclub.http.breeze.response.BreezeHttpResponse;

import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Objects;
import java.util.function.Predicate;

public interface BreezeHttpBodyConverter {
    boolean convert(BreezeHttpRequest request);

    <T> BreezeHttpResponse<T> convert(BreezeHttpRequest request, BreezeHttpRawResponse raw, Type type);

    static void convertRequest(
            Collection<BreezeHttpBodyConverter> converters,
            Predicate<BreezeHttpBodyConverter> predicate
    ) {
        for (BreezeHttpBodyConverter converter : converters) {
            if (!predicate.test(converter)) {
                break;
            }
        }
    }

    static <T> BreezeHttpResponse<T> convertResponse(
            BreezeHttpRequest request,
            BreezeHttpRawResponse raw,
            Type type
    ) {
        return request.breeze().converters().stream()
                .<BreezeHttpResponse<T>>map(converter -> converter.convert(request, raw, type))
                .filter(Objects::nonNull)
                .findFirst()
                .orElseGet(() -> new BreezeHttpResponse<>(raw.convert(type), raw));
    }
}
