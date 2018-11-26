package org.lendingclub.http.breeze.response;

import java.io.Closeable;
import java.io.InputStream;
import java.lang.reflect.Type;

public abstract class BreezeHttpRawResponse extends BreezeHttpResponse<InputStream> implements Closeable {
    protected final Type conversionType;

    public BreezeHttpRawResponse(Type conversionType) {
        this.conversionType = conversionType;
    }

    public Type conversionType() {
        return conversionType;
    }

    public abstract <T> T convert(Type type);

    public abstract String string();

    public abstract byte[] bytes();

    @Override
    public abstract void close();
}
