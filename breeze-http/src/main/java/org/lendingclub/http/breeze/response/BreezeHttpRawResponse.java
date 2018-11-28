package org.lendingclub.http.breeze.response;

import org.lendingclub.http.breeze.converter.BreezeHttpConverter;

import java.io.Closeable;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Objects;

public abstract class BreezeHttpRawResponse extends BreezeHttpResponse<InputStream> implements Closeable {
    protected final List<BreezeHttpConverter> converters;

    public BreezeHttpRawResponse(List<BreezeHttpConverter> converters) {
        this.converters = converters;
    }

    public <T> BreezeHttpResponse<T> convertResponse(Type type) {
        return converters.stream()
                .<BreezeHttpResponse<T>>map(converter -> converter.convertResponse(this, type))
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
