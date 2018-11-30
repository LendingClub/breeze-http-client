package org.lendingclub.http.breeze.response;

import org.lendingclub.http.breeze.request.BreezeHttpRequest;

import java.io.Closeable;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.util.Objects;

public abstract class BreezeHttpRawResponse extends BreezeHttpResponse<InputStream> implements Closeable {
    protected final BreezeHttpRequest request;

    public BreezeHttpRawResponse(BreezeHttpRequest request) {
        this.request = request;
    }

    public <T> BreezeHttpResponse<T> toResponse() {
        return toResponse(request.conversionType());
    }

    public <T> BreezeHttpResponse<T> toResponse(Type type) {
        return request.breeze().converters().stream()
                .<BreezeHttpResponse<T>>map(converter -> converter.toResponse(this, type))
                .filter(Objects::nonNull)
                .findFirst()
                .orElseGet(() -> new BreezeHttpResponse<>(convertBody(type), this));
    }

    public abstract <T> T convertBody(Type type);

    public abstract String string();

    public abstract byte[] bytes();

    @Override
    public abstract void close();
}
