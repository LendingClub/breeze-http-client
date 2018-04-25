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

package org.lendingclub.http.breeze.client.impl.jaxrs.proxy;

import java.util.Map;

import org.lendingclub.http.breeze.client.BreezeHttpClient;

/**
 *
 * @author vreddy
 *
 */
public class BreezeProxyInterfaceClientFactory {
    private BreezeHttpClient breezeHttpClient;

    public BreezeProxyInterfaceClientFactory(BreezeHttpClient breezeHttpClient) {
        this.breezeHttpClient = breezeHttpClient;
    }

    /**
     * @param resourceInterface
     * @param headers
     * @param rootUrl
     * @return
     */
    public <T> T createProxyInterfaceClient(Class<T> resourceInterface, Map<String, String> headers, String rootUrl) {
        return BreezeWebResourceFactory.newResource(resourceInterface, breezeHttpClient, headers,
                new DefaultUrlProvider(rootUrl));
    }

    /**
     * @param resourceInterface
     * @param headers
     * @param urlProvider
     * @return
     */
    public <T> T createProxyInterfaceClient(Class<T> resourceInterface, Map<String, String> headers,
            UrlProvider urlProvider) {
        return BreezeWebResourceFactory.newResource(resourceInterface, breezeHttpClient, headers, urlProvider);
    }

}
