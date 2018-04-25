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

package org.lendingclub.http.breeze.client;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * HTTP response class with status code, headers, and entity.
 *
 * @author Raul Acevedo
 */
public class BreezeHttpResponse<T> {
    private final T entity;
    private final int httpStatusCode;
    private final Map<String, List<String>> headers = new HashMap<>();

    public BreezeHttpResponse() {
        this(null, 0);
    }

    public BreezeHttpResponse(T entity) {
        this(entity, 0);
    }

    public BreezeHttpResponse(T entity, int httpStatusCode) {
        this(entity, httpStatusCode, null);
    }

    public BreezeHttpResponse(T entity, int httpStatusCode, Map<String, List<String>> headers) {
        this.entity = entity;
        this.httpStatusCode = httpStatusCode;
        if (headers != null) {
            headers.forEach((key, value) -> this.headers.put(key, new ArrayList<>(value)));
        }
    }

    public T getEntity() {
        return entity;
    }

    public int getHttpStatusCode() {
        return httpStatusCode;
    }

    public Map<String, List<String>> getHeaders() {
        return headers;
    }

    public List<String> getHeaders(String name) {
        return headers.get(name);
    }

    public String getFirstHeader(String name) {
        List<String> values = headers.get(name);
        return values == null ? null : values.get(0);
    }

    @Override
    public String toString() {
        return "BreezeHttpResponse{entity=" + entity
                + ", httpStatusCode=" + httpStatusCode
                + ", headers=" + headers
                + '}';
    }
}
