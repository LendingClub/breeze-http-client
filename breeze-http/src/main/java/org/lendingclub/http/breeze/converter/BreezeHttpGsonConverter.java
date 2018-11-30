package org.lendingclub.http.breeze.converter;

import org.lendingclub.http.breeze.json.BreezeHttpGsonMapper;
import org.lendingclub.http.breeze.json.BreezeHttpJsonMapper;
import org.lendingclub.http.breeze.request.BreezeHttpRequest;
import org.lendingclub.http.breeze.response.BreezeHttpRawResponse;
import org.lendingclub.http.breeze.response.BreezeHttpResponse;

import java.lang.reflect.Type;

import com.google.gson.JsonElement;

import static org.lendingclub.http.breeze.type.BreezeHttpType.isSubclass;
import static org.lendingclub.http.breeze.util.BreezeHttpUtil.cast;

public class BreezeHttpGsonConverter implements BreezeHttpConverter {
    protected final BreezeHttpJsonMapper gsonMapper;

    public BreezeHttpGsonConverter() {
        this(new BreezeHttpGsonMapper());
    }

    public BreezeHttpGsonConverter(BreezeHttpJsonMapper gsonMapper) {
        this.gsonMapper = gsonMapper;
    }

    @Override
    public boolean convertBody(BreezeHttpRequest request) {
        Object body = request.body();
        if (body != null && isGsonElement(body.getClass())) {
            request.body(body.toString());
        }
        return true;
    }

    @Override
    public <T> BreezeHttpResponse<T> toResponse(BreezeHttpRawResponse raw, Type type) {
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
