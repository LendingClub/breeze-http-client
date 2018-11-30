package org.lendingclub.http.breeze.request.body.builder;

import org.lendingclub.http.breeze.exception.BreezeHttpException;
import org.lendingclub.http.breeze.request.BreezeHttpRequest;
import org.lendingclub.http.breeze.type.BreezeHttpType;

public abstract class AbstractRequestBodyBuilder<B> {
    protected final BreezeHttpRequest request;
    protected final B body;

    public AbstractRequestBodyBuilder(BreezeHttpRequest request, B body) {
        this.request = request;
        this.body = body;
    }

    protected B build() {
        return body;
    }

    protected void finish() {
    }

    public void post() {
        finish();
        request.post(build());
    }

    public <T> T post(Class<T> returnClass) throws BreezeHttpException {
        finish();
        return request.post(build(), returnClass);
    }

    public <T> T post(BreezeHttpType<T> returnType) throws BreezeHttpException {
        finish();
        return request.post(build(), returnType);
    }

    public void put() throws BreezeHttpException {
        finish();
        request.put(build());
    }

    public <T> T put(Class<T> returnClass) throws BreezeHttpException {
        finish();
        return request.put(build(), returnClass);
    }

    public <T> T put(BreezeHttpType<T> returnType) throws BreezeHttpException {
        finish();
        return request.put(build(), returnType);
    }

    public void patch() throws BreezeHttpException {
        finish();
        request.patch(build());
    }

    public <T> T patch(Class<T> returnClass) throws BreezeHttpException {
        finish();
        return request.patch(build(), returnClass);
    }

    public <T> T patch(BreezeHttpType<T> returnType) throws BreezeHttpException {
        finish();
        return request.patch(build(), returnType);
    }

    public void delete() throws BreezeHttpException {
        finish();
        request.delete(build());
    }

    public <T> T delete(Class<T> returnClass) throws BreezeHttpException {
        finish();
        return request.delete(build(), returnClass);
    }

    public <T> T delete(BreezeHttpType<T> returnType) throws BreezeHttpException {
        finish();
        return request.delete(build(), returnType);
    }

    public <T> T execute(Class<T> returnClass) throws BreezeHttpException {
        finish();
        return request.execute(build(), returnClass);
    }

    public <T> T execute(BreezeHttpType<T> returnType) throws BreezeHttpException {
        finish();
        return request.execute(build(), returnType);
    }
}
