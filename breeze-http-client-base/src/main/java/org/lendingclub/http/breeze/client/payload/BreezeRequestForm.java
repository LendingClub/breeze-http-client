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

package org.lendingclub.http.breeze.client.payload;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Represents a form to submit as payload to request.post() or request.put().
 * The client implementation will ensure it is properly encoded and sent with
 * content type application/x-www-form-urlencoded.
 *
 * @author Raul Acevedo
 */
public class BreezeRequestForm {
    private final Map<String, String> params = new LinkedHashMap<>();

    /** Set a form parameter. If value is null, stores empty string. */
    public BreezeRequestForm param(String key, String value) {
        params.put(key, value == null ? "" : value);
        return this;
    }

    public BreezeRequestForm params(Map<String, String> params) {
        this.params.putAll(params);
        return this;
    }

    public Map<String, String> params() {
        return params;
    }
}
