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

import java.util.ArrayList;
import java.util.List;

import org.lendingclub.http.breeze.client.BreezeHttpClient;
import org.lendingclub.http.breeze.client.BreezeHttpRequest;
import org.lendingclub.http.breeze.client.BreezeHttpResponse;
import org.lendingclub.http.breeze.client.BreezeHttpType;
import org.lendingclub.http.breeze.client.exception.BreezeHttpException;
import org.lendingclub.http.breeze.client.filter.BreezeHttpRequestFilter;
import org.lendingclub.http.breeze.client.impl.AbstractBaseClient;
import org.lendingclub.http.breeze.client.matcher.BreezeHttpClientRequestMatcher;

/**
 * Parent class for decorated clients; takes care of all the basics so the
 * decorated implementations can focus on their functionality.
 *
 * @author Raul Acevedo
 */
public abstract class AbstractDecoratedClient extends AbstractBaseClient implements DecoratedClient {
    protected final BreezeHttpClientDecorator decorator;
    protected final BreezeHttpClient client;
    protected final List<BreezeHttpClientRequestMatcher> matchers = new ArrayList<>();
    protected final String decorators;
    protected final BreezeHttpClient implClient;

    public AbstractDecoratedClient(
            BreezeHttpClientDecorator decorator,
            BreezeHttpClient client,
            List<BreezeHttpClientRequestMatcher> matchers
    ) {
        this.decorator = decorator;
        this.client = client;
        this.matchers.addAll(matchers);

        StringBuilder b = new StringBuilder("[").append(decorator.getClass().getSimpleName());
        while (client instanceof DecoratedClient) {
            DecoratedClient commands = (DecoratedClient) client;
            b.append(", ").append(commands.getDecorator().getClass().getSimpleName());
            client = commands.getClient();
        }
        this.decorators = b.append("]").toString();
        this.implClient = client;
    }

    @Override
    public BreezeHttpClientDecorator getDecorator() {
        return decorator;
    }

    @Override
    public BreezeHttpRequest request() {
        return new BreezeHttpRequest(null, null, this, client.getRequestFilters());
    }

    @Override
    public BreezeHttpRequest request(String rootUrl) {
        return new BreezeHttpRequest(rootUrl, null, this, client.getRequestFilters());
    }

    @Override
    public List<BreezeHttpRequestFilter> getRequestFilters() {
        return client.getRequestFilters();
    }

    @Override
    public <T> BreezeHttpResponse<T> execute(BreezeHttpRequest request, BreezeHttpType<T> responseType, Object payload)
            throws BreezeHttpException {
        return execute(request, (commandRequest) -> client.execute(request, responseType, payload));
    }

    /** See if the request matches any matcher for this decorator. */
    protected boolean matches(BreezeHttpRequest request) {
        return matches(matchers, request);
    }

    /** See if the request matches any matcher in the list. */
    protected boolean matches(List<BreezeHttpClientRequestMatcher> matchers, BreezeHttpRequest request) {
        return matchers.stream().anyMatch((matcher) -> matcher.matches(request));
    }

    /** Decorate the command if the request matches, otherwise execute it normally. */
    protected <T> BreezeHttpResponse<T> execute(BreezeHttpRequest request, DecoratorCommand<T> command) {
        if (matches(request)) {
            return decorate(request, command);
        } else {
            return command.execute(request);
        }
    }

    /**
     * Execute a decorated client command.
     *
     * @param request original request object
     * @param command command to execute
     * @param <T> return type
     * @return object of type T
     */
    protected abstract <T> BreezeHttpResponse<T> decorate(BreezeHttpRequest request, DecoratorCommand<T> command);

    @Override
    public BreezeHttpClient getClient() {
        return client;
    }

    @Override
    public BreezeHttpClient getClientImplClass() {
        return implClient;
    }

    /** Splunk-friendly toString: more intuitive to search for BreezeHttpClient than implementation names. */
    @Override
    public String toString() {
        return "BreezeHttpClient{impl=" + implClient.getClass().getSimpleName()
                + ", decorators=" + decorators
                + "}";
    }
}
