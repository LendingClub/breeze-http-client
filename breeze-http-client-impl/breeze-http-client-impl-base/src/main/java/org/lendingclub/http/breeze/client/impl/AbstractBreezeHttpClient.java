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

package org.lendingclub.http.breeze.client.impl;

import org.slf4j.Logger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.lendingclub.http.breeze.client.BreezeHttpRequest;
import org.lendingclub.http.breeze.client.exception.BreezeHttpResponseException;
import org.lendingclub.http.breeze.client.filter.BreezeHttpRequestFilter;

/**
 * Abstract parent class for client implementations.
 *
 * @author Raul Acevedo
 */
public abstract class AbstractBreezeHttpClient extends AbstractBaseClient {
    protected final List<BreezeHttpRequestFilter> requestFilters = new ArrayList<>();
    protected final Logger logger;

    public AbstractBreezeHttpClient(List<BreezeHttpRequestFilter> requestFilters, Logger logger) {
        this.requestFilters.addAll(requestFilters);
        this.logger = logger;
    }

    @Override
    public List<BreezeHttpRequestFilter> getRequestFilters() {
        return requestFilters;
    }

    protected void logRequestStart(BreezeHttpRequest request) {
        if (logger != null && logger.isInfoEnabled()) {
            logger.info("executing {}", request);
        }
    }

    protected void logRequestEnd(BreezeHttpRequest request, long startTime) {
        if (logger != null && logger.isInfoEnabled()) {
            logger.info("successfully executed {}, timing={} ms", request, System.currentTimeMillis() - startTime);
        }
    }

    protected void logRequestException(BreezeHttpRequest request, long startTime, Exception e) {
        if (logger != null) {
            String message = e.getClass().getSimpleName() + " executing " + request;

            IOException networkError = findNetworkError(e);
            if (networkError == null) {
                message += ", isNetworkError=false";
            } else {
                message += ", isNetworkError=true, networkError=" + networkError.getClass().getSimpleName();
            }

            BreezeHttpResponseException breezeException = null;
            if (e instanceof BreezeHttpResponseException) {
                breezeException = (BreezeHttpResponseException) e;
                message += ", httpStatusCode=" + breezeException.getHttpStatusCode();
            }

            message += ", timing=" + (System.currentTimeMillis() - startTime) + " ms";
            if (breezeException != null && breezeException.getHttpStatusCode() / 100 == 4) {
                logger.warn(message);
            } else {
                logger.error(message);
            }
        }
    }

    private IOException findNetworkError(Exception e) {
        Throwable t = e;
        while (t != null) {
            if (t instanceof IOException) {
                return (IOException) t;
            }
            t = t.getCause();
        }
        return null;
    }

    /** Splunk-friendly toString: more intuitive to search for BreezeHttpClient than implementation names. */
    @Override
    public String toString() {
        return "BreezeHttpClient{impl=" + getClass().getSimpleName() + "}";
    }
}
