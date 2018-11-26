package org.lendingclub.http.breeze.request.body.builder;

import org.lendingclub.http.breeze.request.BreezeHttpRequest;

import java.util.LinkedHashMap;
import java.util.Map;

public class MapRequestBodyBuilder extends AbstractRequestBodyBuilder<Map<Object, Object>> {
    public MapRequestBodyBuilder(BreezeHttpRequest request) {
        super(request, new LinkedHashMap<>());
    }

    public MapRequestBodyBuilder entry(Object key, Object value) {
        body.put(key, value);
        return this;
    }

    public MapRequestBodyBuilder entries(Map<?, ?> map) {
        body.putAll(map);
        return this;
    }
}
