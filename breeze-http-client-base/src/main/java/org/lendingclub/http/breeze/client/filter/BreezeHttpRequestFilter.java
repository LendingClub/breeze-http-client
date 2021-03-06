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

package org.lendingclub.http.breeze.client.filter;

import org.lendingclub.http.breeze.client.BreezeHttpRequest;

/**
 * Filter that can modify a request object when it is first constructed or
 * immediately before the request is executed.
 *
 * @author Raul Acevedo
 */
public interface BreezeHttpRequestFilter {
    default void prepareRequest(BreezeHttpRequest request) {
    }

    default void finalizeRequest(BreezeHttpRequest request) {
    }
}
