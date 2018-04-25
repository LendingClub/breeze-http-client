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

package org.lendingclub.http.breeze.client.matcher;

import org.lendingclub.http.breeze.client.BreezeHttpRequest;

import static org.lendingclub.http.breeze.client.BreezeHttpRequest.Method;

/**
 * Matches GET requests.
 *
 * @author Raul Acevedo
 */
public class QueryRequestMatcher implements BreezeHttpClientRequestMatcher {
    @Override
    public boolean matches(BreezeHttpRequest request) {
        return Method.GET.equals(request.getMethod());
    }
}
