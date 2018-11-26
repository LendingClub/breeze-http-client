package org.lendingclub.http.breeze.converter;

import org.lendingclub.http.breeze.client.json.BreezeHttpGsonMapper;
import org.lendingclub.http.breeze.client.json.BreezeHttpJsonMapper;
import org.lendingclub.http.breeze.request.BreezeHttpRequest;
import org.lendingclub.http.breeze.response.BreezeHttpRawResponse;
import org.lendingclub.http.breeze.response.BreezeHttpResponse;

import java.lang.reflect.Type;

import com.google.gson.JsonElement;

import static org.lendingclub.http.breeze.BreezeHttpType.isSubclass;
import static org.lendingclub.http.breeze.util.BreezeHttpUtil.cast;

public class BreezeHttpGsonTypesConverter implements BreezeHttpBodyConverter {
    protected final BreezeHttpJsonMapper gsonMapper;

    public BreezeHttpGsonTypesConverter() {
        this(new BreezeHttpGsonMapper());
    }

    public BreezeHttpGsonTypesConverter(BreezeHttpJsonMapper gsonMapper) {
        this.gsonMapper = gsonMapper;
    }

    @Override
    public boolean convert(BreezeHttpRequest request) {
        Object body = request.body();
        if (body != null && isGsonElement(body.getClass())) {
            request.body(body.toString());
        }
        return true;
    }

    @Override
    public <T> BreezeHttpResponse<T> convert(BreezeHttpRequest request, BreezeHttpRawResponse raw, Type type) {
        if (isGsonElement(type)) {
            return cast(new BreezeHttpResponse<>(gsonMapper.parse(raw.body()), raw));
        } else {
            return null;
        }
    }

    protected boolean isGsonElement(Type type) {
        return isSubclass(JsonElement.class, type);
    }
}
