package org.lendingclub.http.breeze.converter;

import org.lendingclub.http.breeze.client.json.BreezeHttpJacksonMapper;
import org.lendingclub.http.breeze.client.json.BreezeHttpJsonMapper;
import org.lendingclub.http.breeze.request.BreezeHttpRequest;
import org.lendingclub.http.breeze.response.BreezeHttpRawResponse;
import org.lendingclub.http.breeze.response.BreezeHttpResponse;

import java.lang.reflect.Type;

import com.fasterxml.jackson.databind.JsonNode;

import static org.lendingclub.http.breeze.BreezeHttpType.isSubclass;
import static org.lendingclub.http.breeze.util.BreezeHttpUtil.cast;

public class BreezeHttpJacksonTypesConverter implements BreezeHttpBodyConverter {
    protected final BreezeHttpJsonMapper jacksonMapper;

    public BreezeHttpJacksonTypesConverter() {
        this(new BreezeHttpJacksonMapper());
    }

    public BreezeHttpJacksonTypesConverter(BreezeHttpJsonMapper jacksonMapper) {
        this.jacksonMapper = jacksonMapper;
    }

    @Override
    public boolean convert(BreezeHttpRequest request) {
        Object body = request.body();
        if (body != null && isJacksonNode(body.getClass())) {
            request.body(body.toString());
        }
        return true;
    }

    @Override
    public <T> BreezeHttpResponse<T> convert(BreezeHttpRequest request, BreezeHttpRawResponse raw, Type type) {
        if (isJacksonNode(type)) {
            return cast(new BreezeHttpResponse<>(jacksonMapper.parse(raw.body()), raw));
        } else {
            return null;
        }
    }

    protected boolean isJacksonNode(Type type) {
        return isSubclass(JsonNode.class, type);
    }
}
