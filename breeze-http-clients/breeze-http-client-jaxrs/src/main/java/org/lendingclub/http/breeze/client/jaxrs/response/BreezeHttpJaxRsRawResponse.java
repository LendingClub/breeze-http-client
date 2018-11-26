package org.lendingclub.http.breeze.client.jaxrs.response;

import org.lendingclub.http.breeze.response.BreezeHttpRawResponse;
import org.lendingclub.http.breeze.response.BreezeHttpResponseInputStream;

import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.Response;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.util.ArrayList;

import static org.lendingclub.http.breeze.util.BreezeHttpUtil.cast;

public class BreezeHttpJaxRsRawResponse extends BreezeHttpRawResponse {
    private final Response response;
    private final boolean hasEntity;

    public BreezeHttpJaxRsRawResponse(Type conversionType, Response response) {
        super(conversionType);
        this.response = response;
        this.hasEntity = response.hasEntity();
        this.httpStatus = response.getStatus();

        response.getStringHeaders().forEach((key, values) -> headers.put(key, new ArrayList<>(values)));
    }

    @Override
    public InputStream body() {
        return response.readEntity(InputStream.class);
    }

    @Override
    public String string() {
        return hasEntity ? response.readEntity(String.class) : null;
    }

    @Override
    public byte[] bytes() {
        return response.readEntity(byte[].class);
    }

    @Override
    public <T> T convert(Type type) {
        if (!hasEntity || type == Void.class || type == null) {
            return null;
        }

        if (type == InputStream.class) {
            return cast(new BreezeHttpResponseInputStream(this, response.readEntity(InputStream.class)));
        }

        return response.readEntity(new GenericType<>(type));
    }

    @Override
    public void close() {
        response.close();
    }
}
