package org.lendingclub.http.breeze.json;

import org.lendingclub.http.breeze.exception.BreezeHttpException;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

public class BreezeHttpJacksonMapper implements BreezeHttpJsonMapper {
    private final ObjectMapper crapper;

    public BreezeHttpJacksonMapper() {
        this(false);
    }

    public BreezeHttpJacksonMapper(boolean prettyPrint) {
        this.crapper = new ObjectMapper()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false)
                .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
                .setSerializationInclusion(JsonInclude.Include.NON_NULL);
        if (prettyPrint) {
            this.crapper.enable(SerializationFeature.INDENT_OUTPUT);
        }
    }

    public BreezeHttpJacksonMapper(ObjectMapper crapper) {
        this.crapper = crapper;
    }

    @Override
    public String toJson(Object o) {
        try {
            return crapper.writeValueAsString(o);
        } catch (JsonProcessingException e) {
            throw new BreezeHttpException(e);
        }
    }

    @Override
    public <T> T fromJson(String s, Type type) {
        try {
            return crapper.readValue(s, new TypeReference<T>() {
                @Override
                public Type getType() {
                    return type;
                }
            });
        } catch (IOException e) {
            throw new BreezeHttpException(e);
        }
    }

    @Override
    public Object parse(String json) {
        try {
            return crapper.readTree(json);
        } catch (IOException e) {
            throw new BreezeHttpException(e);
        }
    }

    @Override
    public Object parse(InputStream stream) {
        try {
            return crapper.readTree(stream);
        } catch (IOException e) {
            throw new BreezeHttpException(e);
        }
    }
}
