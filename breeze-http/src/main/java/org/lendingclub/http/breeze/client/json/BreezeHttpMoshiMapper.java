package org.lendingclub.http.breeze.client.json;

import org.lendingclub.http.breeze.exception.BreezeHttpException;
import org.lendingclub.http.breeze.util.BreezeHttpUtil;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;

import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;

import static org.lendingclub.http.breeze.util.BreezeHttpUtil.isPresent;

public class BreezeHttpMoshiMapper implements BreezeHttpJsonMapper {
    public static final boolean IS_MOSHI_PRESENT = isPresent("com.squareup.moshi.Moshi");

    private final Moshi moshi;
    private final boolean prettyPrint;

    public BreezeHttpMoshiMapper() {
        this(false);
    }

    public BreezeHttpMoshiMapper(boolean prettyPrint) {
        this.moshi = new Moshi.Builder().build();
        this.prettyPrint = prettyPrint;
    }

    public BreezeHttpMoshiMapper(Moshi moshi) {
        this.moshi = moshi;
        this.prettyPrint = false;
    }

    @Override
    public String toJson(Object o) {
        if (o instanceof InputStream) {
            o = BreezeHttpUtil.readString((InputStream) o);
        }

        JsonAdapter<Object> adapter = moshi.adapter(Object.class);
        if (prettyPrint) {
            adapter = adapter.indent("  ");
        }
        return adapter.toJson(o);
    }

    @Override
    public <T> T fromJson(String s, Type type) {
        try {
            JsonAdapter<T> adapter = moshi.adapter(type);
            return adapter.fromJson(s);
        } catch (IOException e) {
            throw new BreezeHttpException(e);
        }
    }

    @Override
    public Object parse(String json) {
        return fromJson(json, Object.class);
    }

    @Override
    public Object parse(InputStream stream) {
        return parse(BreezeHttpUtil.readString(stream));
    }
}
