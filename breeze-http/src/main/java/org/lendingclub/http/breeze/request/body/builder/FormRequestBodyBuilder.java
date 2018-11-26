package org.lendingclub.http.breeze.request.body.builder;

import org.lendingclub.http.breeze.request.BreezeHttpRequest;
import org.lendingclub.http.breeze.request.body.BreezeHttpForm;

public class FormRequestBodyBuilder extends AbstractRequestBodyBuilder<BreezeHttpForm> {
    public FormRequestBodyBuilder(BreezeHttpRequest request) {
        super(request, new BreezeHttpForm());
    }

    public FormRequestBodyBuilder param(String key, String value) {
        body.param(key, value);
        return this;
    }
}
