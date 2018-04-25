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

package org.lendingclub.http.breeze.client.decorator;

import org.lendingclub.http.breeze.client.BreezeHttpClient;
import org.lendingclub.http.breeze.client.BreezeHttpRequest;
import org.lendingclub.http.breeze.client.BreezeHttpResponse;
import org.lendingclub.http.breeze.client.matcher.AllRequestMatcher;

import static java.util.Collections.singletonList;

/**
 * Decorator that sets the target endpoint, rootUrl and remoteService, for every request.
 *
 * @author Raul Acevedo
 */
public class EndpointDecorator implements BreezeHttpClientDecorator {
    private final String rootUrl;
    private final String remoteService;

    public EndpointDecorator(String rootUrl, String remoteService) {
        this.rootUrl = rootUrl;
        this.remoteService = remoteService;
    }

    @Override
    public BreezeHttpClient decorate(BreezeHttpClient client) {
        return new EndpointDecoratedClient(client);
    }

    public class EndpointDecoratedClient extends AbstractDecoratedClient {
        public EndpointDecoratedClient(BreezeHttpClient client) {
            super(EndpointDecorator.this, client, singletonList(new AllRequestMatcher()));
        }

        @Override
        protected <T> BreezeHttpResponse<T> decorate(BreezeHttpRequest request, DecoratorCommand<T> command) {
            if (request.getRootUrl() == null) {
                request.rootUrl(rootUrl);
            }

            if (request.getRemoteService() == null) {
                request.remoteService(remoteService);
            }

            return command.execute(request);
        }
    }
}
