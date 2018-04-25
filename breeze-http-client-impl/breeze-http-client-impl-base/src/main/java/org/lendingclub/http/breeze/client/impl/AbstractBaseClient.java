/*
 * Copyright (C) 2018 Lending Club, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.lendingclub.http.breeze.client.impl;

import org.lendingclub.http.breeze.client.BreezeHttpClient;
import org.lendingclub.http.breeze.client.BreezeHttpRequest;
import org.lendingclub.http.breeze.client.BreezeHttpResponse;
import org.lendingclub.http.breeze.client.BreezeHttpType;
import org.lendingclub.http.breeze.client.decorator.EndpointDecorator;
import org.lendingclub.http.breeze.client.exception.BreezeHttpException;

import static org.lendingclub.http.breeze.client.BreezeHttpRequest.Method.GET;
import static org.lendingclub.http.breeze.client.BreezeHttpRequest.Method.PATCH;
import static org.lendingclub.http.breeze.client.BreezeHttpRequest.Method.POST;
import static org.lendingclub.http.breeze.client.BreezeHttpRequest.Method.PUT;

/**
 * Base implementation of BreezeHttpClient; can be used by decorators or real
 * invoking implementations. This class only delegates methods to the main
 * execute method.
 *
 * @author Raul Acevedo
 */
public abstract class AbstractBaseClient implements BreezeHttpClient {
    @Override
    public BreezeHttpRequest request() {
        return new BreezeHttpRequest(null, null, this, getRequestFilters());
    }

    @Override
    public BreezeHttpRequest request(String rootUrl) {
        return new BreezeHttpRequest(rootUrl, this);
    }

    @Override
    public void get(BreezeHttpRequest request) throws BreezeHttpException {
        get(request, Void.class);
    }

    @Override
    public <T> T get(BreezeHttpRequest request, Class<T> responseClass) throws BreezeHttpException {
        return execute(request.method(GET), responseClass, null).getEntity();
    }

    @Override
    public <T> T get(BreezeHttpRequest request, BreezeHttpType<T> responseType) throws BreezeHttpException {
        return execute(request.method(GET), responseType, null).getEntity();
    }

    @Override
    public void post(BreezeHttpRequest request, Object payload) throws BreezeHttpException {
        execute(request.method(POST), Void.class, payload);
    }

    @Override
    public <T> T post(BreezeHttpRequest request, Class<T> responseClass, Object payload) throws BreezeHttpException {
        return execute(request.method(POST), responseClass, payload).getEntity();
    }

    @Override
    public <T> T post(BreezeHttpRequest request, BreezeHttpType<T> responseType, Object payload) throws BreezeHttpException {
        return execute(request.method(POST), responseType, payload).getEntity();
    }

    @Override
    public void put(BreezeHttpRequest request, Object payload) throws BreezeHttpException {
        execute(request.method(PUT), Void.class, payload);
    }

    @Override
    public <T> T put(BreezeHttpRequest request, Class<T> responseClass, Object payload) throws BreezeHttpException {
        return execute(request.method(PUT), responseClass, payload).getEntity();
    }

    @Override
    public <T> T put(BreezeHttpRequest request, BreezeHttpType<T> responseType, Object payload) throws BreezeHttpException {
        return execute(request.method(PUT), responseType, payload).getEntity();
    }

    @Override
    public void patch(BreezeHttpRequest request, Object payload) throws BreezeHttpException {
        execute(request.method(PATCH), Void.class, payload);
    }

    @Override
    public <T> T patch(BreezeHttpRequest request, Class<T> responseClass, Object payload) throws BreezeHttpException {
        return execute(request.method(PATCH), responseClass, payload).getEntity();
    }

    @Override
    public <T> T patch(BreezeHttpRequest request, BreezeHttpType<T> responseType, Object payload)
            throws BreezeHttpException {
        return execute(request.method(PATCH), responseType, payload).getEntity();
    }

    @Override
    public <T> BreezeHttpResponse<T> execute(BreezeHttpRequest request, Class<T> responseClass, Object payload)
            throws BreezeHttpException {
        return execute(request, new BreezeHttpType<>(responseClass), payload);
    }

    @Override
    public BreezeHttpClient forService(String rootUrl, String remoteService) {
        return new EndpointDecorator(rootUrl, remoteService).decorate(this);
    }
}
