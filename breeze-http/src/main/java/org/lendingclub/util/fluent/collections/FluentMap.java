package org.lendingclub.util.fluent.collections;

import java.util.LinkedHashMap;
import java.util.Map;

public class FluentMap extends LinkedHashMap<Object, Object> {
    private static final long serialVersionUID = -1;

    public static FluentMap map() {
        return new FluentMap();
    }

    public static FluentMap map(Object key, Object value) {
        return new FluentMap(key, value);
    }

    public FluentMap() {
    }

    public FluentMap(Object key, Object value) {
        put(key, value);
    }

    public FluentMap(Map<?, ?> map) {
        super(map);
    }

    public FluentMap entry(Object key, Object value) {
        put(key, value);
        return this;
    }

    public FluentMap entries(Map<?, ?> map) {
        putAll(map);
        return this;
    }
}
