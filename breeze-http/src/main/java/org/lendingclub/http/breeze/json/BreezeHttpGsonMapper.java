package org.lendingclub.http.breeze.json;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParser;

public class BreezeHttpGsonMapper implements BreezeHttpJsonMapper {
    private final Gson gson;
    private final JsonParser parser = new JsonParser();

    public BreezeHttpGsonMapper() {
        this(false);
    }

    public BreezeHttpGsonMapper(boolean prettyPrint) {
        this.gson = prettyPrint
                ? new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create()
                : new Gson();
    }

    public BreezeHttpGsonMapper(Gson gson) {
        this.gson = gson;
    }

    @Override
    public String toJson(Object o) {
        return gson.toJson(o);
    }

    @Override
    public <T> T fromJson(String s, Type type) {
        return gson.fromJson(s, type);
    }

    @Override
    public Object parse(String json) {
        return parser.parse(json);
    }

    @Override
    public Object parse(InputStream stream) {
        return parser.parse(new InputStreamReader(stream));
    }
}
