package org.lendingclub.http.breeze.request.body.builder;

import org.lendingclub.http.breeze.request.BreezeHttpRequest;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ListRequestBodyBuilder extends AbstractRequestBodyBuilder<List<Object>> {
    public ListRequestBodyBuilder(BreezeHttpRequest request) {
        super(request, new ArrayList<>());
    }

    public ListRequestBodyBuilder item(Object item) {
        body.add(item);
        return this;
    }

    public ListRequestBodyBuilder items(List<?> items) {
        body.addAll(items);
        return this;
    }

    public ListRequestBodyBuilder items(Object... items) {
        Collections.addAll(body, items);
        return this;
    }
}
