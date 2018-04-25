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

import org.lendingclub.http.breeze.client.BreezeHttpClient;
import org.lendingclub.http.breeze.client.BreezeHttpRequest;
import org.lendingclub.http.breeze.client.decorator.DecoratedClient;

/**
 * Filter to set the User-Agent header for every request.
 *
 * @author Raul Acevedo
 */
public class UserAgentRequestFilter implements BreezeHttpRequestFilter {
    @Override
    public void prepareRequest(BreezeHttpRequest request) {
        Class<? extends BreezeHttpClient> implClass;
        if (request.getClient() instanceof DecoratedClient) {
            implClass = ((DecoratedClient) request.getClient()).getClientImplClass().getClass();
        } else {
            implClass = request.getClient().getClass();
        }
        String implName = implClass.getSimpleName();
        String implVersion = implClass.getPackage().getImplementationVersion(); // returns null inside intellij

        request.header("User-Agent", "BreezeHttp/" + implVersion + " (" + implName + ")", true);
    }
}
