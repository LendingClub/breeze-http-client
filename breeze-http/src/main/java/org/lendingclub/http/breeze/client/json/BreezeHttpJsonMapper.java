package org.lendingclub.http.breeze.client.json;

import java.io.InputStream;
import java.lang.reflect.Type;

import static org.lendingclub.http.breeze.client.json.BreezeHttpMoshiMapper.IS_MOSHI_PRESENT;
import static org.lendingclub.http.breeze.util.BreezeHttpUtil.IS_GSON_PRESENT;
import static org.lendingclub.http.breeze.util.BreezeHttpUtil.IS_JACKSON_PRESENT;

public interface BreezeHttpJsonMapper {
    String toJson(Object o);

    <T> T fromJson(String s, Type type);

    Object parse(String json);

    Object parse(InputStream stream);

    static BreezeHttpJsonMapper findMapper() {
        return findMapper(false);
    }

    static BreezeHttpJsonMapper findMapper(boolean prettyPrint) {
        if (IS_GSON_PRESENT) {
            return new BreezeHttpGsonMapper(prettyPrint);
        } else if (IS_JACKSON_PRESENT) {
            return new BreezeHttpJacksonMapper(prettyPrint);
        } else if (IS_MOSHI_PRESENT) {
            return new BreezeHttpMoshiMapper(prettyPrint);
        } else {
            return new BreezeHttpStringJsonMapper();
        }
    }
}
