package org.lendingclub.http.breeze.client.json;

import org.lendingclub.http.breeze.util.BreezeHttpUtil;

import java.io.InputStream;
import java.lang.reflect.Type;

import static org.lendingclub.http.breeze.util.BreezeHttpUtil.cast;

public class BreezeHttpStringJsonMapper implements BreezeHttpJsonMapper {
    @Override
    public String toJson(Object o) {
        if (o instanceof InputStream) {
            return BreezeHttpUtil.readString((InputStream) o);
        } else {
            return String.valueOf(o);
        }
    }

    @Override
    public <T> T fromJson(String s, Type type) {
        if (type == String.class) {
            return cast(s);
        } else {
            throw new IllegalStateException("cannot convert to type=" + type);
        }
    }

    @Override
    public Object parse(String json) {
        return json;
    }

    @Override
    public Object parse(InputStream stream) {
        return BreezeHttpUtil.readString(stream);
    }
}
