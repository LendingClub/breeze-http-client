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

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.lendingclub.http.breeze.client.BreezeHttpRequest;

import static org.lendingclub.http.breeze.client.BreezeHttpRequest.Method;

/**
 * Matches "command" HTTP requests that will modify data on the server: PUT,
 * POST, PATCH and DELETE.
 *
 * @author Raul Acevedo
 */
public class CommandRequestMatcher implements BreezeHttpClientRequestMatcher {
    private static final Set<Method> COMMAND_METHODS = new HashSet<>(Arrays.asList(
            Method.PUT,
            Method.POST,
            Method.PATCH,
            Method.DELETE
    ));

    @Override
    public boolean matches(BreezeHttpRequest request) {
        return COMMAND_METHODS.contains(request.getMethod());
    }
}
