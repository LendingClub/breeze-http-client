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

package org.lendingclub.http.breeze.client.decorator.retry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import org.lendingclub.http.breeze.client.BreezeHttpClient;
import org.lendingclub.http.breeze.client.BreezeHttpRequest;
import org.lendingclub.http.breeze.client.BreezeHttpResponse;
import org.lendingclub.http.breeze.client.BreezeHttpType;
import org.lendingclub.http.breeze.client.decorator.AbstractDecoratedClient;
import org.lendingclub.http.breeze.client.decorator.BreezeHttpClientDecorator;
import org.lendingclub.http.breeze.client.decorator.DecoratorCommand;
import org.lendingclub.http.breeze.client.exception.BreezeHttpException;
import org.lendingclub.http.breeze.client.exception.BreezeHttpResponseException;
import org.lendingclub.http.breeze.client.matcher.AllRequestMatcher;
import org.lendingclub.http.breeze.client.matcher.BreezeHttpClientRequestMatcher;

/**
 * Simple retry decorator; takes a list of millisecond delays to pause between
 * recoverable failures.
 *
 * @author Raul Acevedo
 */
public class RetryDecorator implements BreezeHttpClientDecorator {
    private static final Logger LOGGER = LoggerFactory.getLogger(RetryDecorator.class);

    private final List<BreezeHttpClientRequestMatcher> matchers = new ArrayList<>();
    private final List<Long> retryDelays = new ArrayList<>();
    private final Sleeper sleeper;

    public RetryDecorator(long... retryDelays) {
        this(Collections.singletonList(new AllRequestMatcher()),
                Arrays.stream(retryDelays).boxed().collect(Collectors.toList()));
    }

    public RetryDecorator(List<Long> retryDelays) {
        this(Collections.singletonList(new AllRequestMatcher()), retryDelays);
    }

    public RetryDecorator(List<BreezeHttpClientRequestMatcher> matchers, List<Long> retryDelays) {
        this(matchers, retryDelays, new Sleeper());
    }

    public RetryDecorator(
            List<BreezeHttpClientRequestMatcher> matchers,
            List<Long> retryDelays,
            Sleeper sleeper
    ) {
        this.matchers.addAll(matchers);
        this.retryDelays.addAll(retryDelays);
        this.sleeper = sleeper;
    }

    @Override
    public BreezeHttpClient decorate(BreezeHttpClient client) {
        return new RetryDecoratedClient(client, matchers);
    }

    /** This class exists for unit tests, but maybe somebody someday will want to override it. */
    public static class Sleeper {
        public void sleep(long milliseconds) throws InterruptedException {
            Thread.sleep(milliseconds);
        }
    }

    public class RetryDecoratedClient extends AbstractDecoratedClient {
        RetryDecoratedClient(BreezeHttpClient client, List<BreezeHttpClientRequestMatcher> matchers) {
            super(RetryDecorator.this, client, matchers);
        }

        /** Retry is not possible with InputStream because streams can only be read once. */
        @Override
        public <T> BreezeHttpResponse<T> execute(BreezeHttpRequest request, BreezeHttpType<T> responseType, Object payload)
                throws BreezeHttpException {
            if (payload instanceof InputStream) {
                try {
                    return client.execute(request, responseType, payload);
                } catch (BreezeHttpResponseException e) {
                    if (e.getHttpStatusCode() / 100 == 5 && !retryDelays.isEmpty()) {
                        // Only warn if we would have retried: recoverable error and retryDelays exist
                        LOGGER.warn("recoverable " + e.getClass() + " but cannot retry stream for " + request);
                    }
                    throw e;
                }
            } else {
                return execute(request, (commandRequest) -> client.execute(request, responseType, payload));
            }
        }

        @Override
        protected <T> BreezeHttpResponse<T> decorate(BreezeHttpRequest request, DecoratorCommand<T> command) {
            Iterator<Long> iter = retryDelays.iterator();

            while (true) {
                try {
                    // Make sure to pass in a copy of the request since execution
                    // may modify the request object and we don't want it altered
                    // unintentionally the same way twice.
                    return command.execute(new BreezeHttpRequest(request));
                } catch (BreezeHttpResponseException e) {
                    String errorName = e.getClass().getSimpleName();
                    if (e.getHttpStatusCode() / 100 == 5 && iter.hasNext()) {
                        // Recoverable error and with delays left, so sleep and retry
                        long delay = iter.next();
                        LOGGER.warn("pausing after recoverable " + errorName
                                + " for delay=" + delay + " ms"
                                + " then retrying " + request);
                        try {
                            sleeper.sleep(delay);
                        } catch (InterruptedException interruptedException) {
                            throw new BreezeHttpException("thread interrupted retrying " + request, interruptedException);
                        }
                    } else {
                        // Unrecoverable error or we ran out of retries
                        if (e.getHttpStatusCode() / 100 == 5 && !iter.hasNext()) {
                            LOGGER.warn("recoverable error " + errorName + " but out of retries, giving up on " + request);
                        }
                        throw e;
                    }
                } catch (Exception e) {
                    throw new BreezeHttpException("unexpected error retrying " + request, e);
                }
            }
        }
    }
}
