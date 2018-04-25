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

import java.util.LinkedHashMap;
import java.util.Map;

import org.lendingclub.http.breeze.client.BreezeHttpRequest;

/**
 * Builder for map request body.
 *
 * @author Raul Acevedo
 */
public class MapPayloadBuilderRequest extends AbstractPayloadBuilderRequest<Map<Object, Object>> {
    public MapPayloadBuilderRequest(BreezeHttpRequest request) {
        super(request, new LinkedHashMap<>());
    }

    public MapPayloadBuilderRequest entry(Object key, Object value) {
        payload.put(key, value);
        return this;
    }

    public MapPayloadBuilderRequest entries(Map<?, ?> map) {
        payload.putAll(map);
        return this;
    }
}
