package org.lendingclub.http.breeze.request.body;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.lendingclub.http.breeze.util.BreezeHttpUtil.quote;

/**
 * Represents a form to submit in a POST or PUT request. The client
 * implementation will ensure it is properly encoded and sent with content
 * type application/x-www-form-urlencoded.
 */
public class BreezeHttpForm {
    private final Map<String, String> params = new LinkedHashMap<>();

    public Map<String, String> params() {
        return params;
    }

    public String param(String key) {
        return params.get(key);
    }

    /** Set a form parameter. If value is null, stores empty string. */
    public BreezeHttpForm param(String key, String value) {
        params.put(key, value == null ? "" : value);
        return this;
    }

    public BreezeHttpForm params(Map<String, String> params) {
        this.params.putAll(params);
        return this;
    }

    @Override
    public String toString() {
        return "BreezeHttpForm{" + "params=" + quote(params.keySet()) + '}';
    }
}
