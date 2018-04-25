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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.lendingclub.http.breeze.client.BreezeHttpRequest;

/**
 * Builder for list request body.
 *
 * @author Raul Acevedo
 */
public class ListPayloadBuilderRequest extends AbstractPayloadBuilderRequest<List<Object>> {
    public ListPayloadBuilderRequest(BreezeHttpRequest request) {
        super(request, new ArrayList<>());
    }

    public ListPayloadBuilderRequest item(Object item) {
        payload.add(item);
        return this;
    }

    public ListPayloadBuilderRequest items(List<?> items) {
        payload.addAll(items);
        return this;
    }

    public ListPayloadBuilderRequest items(Object... items) {
        Collections.addAll(payload, items);
        return this;
    }
}
