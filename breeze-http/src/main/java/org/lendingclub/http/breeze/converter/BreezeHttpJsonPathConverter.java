package org.lendingclub.http.breeze.converter;

import org.lendingclub.http.breeze.request.BreezeHttpRequest;
import org.lendingclub.http.breeze.response.BreezeHttpRawResponse;
import org.lendingclub.http.breeze.response.BreezeHttpResponse;

import java.lang.reflect.Type;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;

import static org.lendingclub.http.breeze.type.BreezeHttpType.isSubclass;
import static org.lendingclub.http.breeze.util.BreezeHttpUtil.cast;

public class BreezeHttpJsonPathConverter implements BreezeHttpConverter {
    @Override
    public boolean convertBody(BreezeHttpRequest request) {
        Object body = request.body();
        if (body != null && isJsonPathDocument(body.getClass())) {
            request.body(((DocumentContext) body).read("$"));
        }
        return true;
    }

    @Override
    public <T> BreezeHttpResponse<T> toResponse(BreezeHttpRawResponse raw, Type type) {
        if (isJsonPathDocument(type)) {
            return cast(new BreezeHttpResponse<>(JsonPath.parse(raw.body()), raw));
        } else {
            return null;
        }
    }

    protected boolean isJsonPathDocument(Type type) {
        return isSubclass(DocumentContext.class, type);
    }
}
