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

package org.lendingclub.http.breeze.client.payload.builder;

import org.lendingclub.http.breeze.client.BreezeHttpRequest;
import org.lendingclub.http.breeze.client.BreezeHttpResponse;
import org.lendingclub.http.breeze.client.BreezeHttpType;
import org.lendingclub.http.breeze.client.exception.BreezeHttpException;

/**
 * Parent class for request body payloads of any type.
 * @param <P> the request body type
 *
 * @author Raul Acevedo
 */
public abstract class AbstractPayloadBuilderRequest<P> {
    protected final BreezeHttpRequest request;
    protected final P payload;

    public AbstractPayloadBuilderRequest(BreezeHttpRequest request, P payload) {
        this.request = request;
        this.payload = payload;
    }

    protected P buildPayload() {
        return payload;
    }

    public void post() {
        request.post(buildPayload());
    }

    public <T> T post(Class<T> responseType) throws BreezeHttpException {
        return request.post(responseType, buildPayload());
    }

    public <T> T post(BreezeHttpType<T> genericType) throws BreezeHttpException {
        return request.post(genericType, buildPayload());
    }

    public void put() throws BreezeHttpException {
        request.put(buildPayload());
    }

    public <T> T put(Class<T> responseType) throws BreezeHttpException {
        return request.put(responseType, buildPayload());
    }

    public <T> T put(BreezeHttpType<T> genericType) throws BreezeHttpException {
        return request.put(genericType, buildPayload());
    }

    public <T> BreezeHttpResponse<T> execute(Class<T> responseClass) throws BreezeHttpException {
        return request.execute(responseClass, buildPayload());
    }

    public <T> BreezeHttpResponse<T> execute(BreezeHttpType<T> responseType) throws BreezeHttpException {
        return request.execute(responseType, buildPayload());
    }
}
