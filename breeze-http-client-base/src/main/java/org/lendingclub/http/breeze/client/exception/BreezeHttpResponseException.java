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

package org.lendingclub.http.breeze.client.exception;

import org.lendingclub.http.breeze.client.BreezeHttpRequest;
import org.lendingclub.http.breeze.client.BreezeHttpResponse;

import java.util.List;
import java.util.Map;

/**
 * Exception when there was a server response.
 *
 * @author Raul Acevedo
 */
public class BreezeHttpResponseException extends BreezeHttpException {
    public static final long serialVersionUID = -1;

    private final BreezeHttpRequest request;
    private final BreezeHttpResponse<?> response;
    private final Map<String, List<String>> headers;

    public BreezeHttpResponseException(String message, BreezeHttpRequest request, BreezeHttpResponse<?> response) {
        super(message);
        this.request = request;
        this.response = response;
        this.headers = response.getHeaders();
    }

    public BreezeHttpResponseException(
            String message,
            Throwable cause,
            BreezeHttpRequest request,
            BreezeHttpResponse<?> response
    ) {
        super(message, cause);
        this.request = request;
        this.response = response;
        this.headers = response.getHeaders();
    }

    public BreezeHttpResponseException(
            String message,
            int httpStatusCode,
            Map<String, List<String>> headers
    ) {
        super(message);
        this.headers = headers;
        this.request = null;
        this.response = null;
    }

    public BreezeHttpRequest getRequest() {
        return request;
    }

    public BreezeHttpResponse<?> getResponse() {
        return response;
    }

    public String getFirstHeader(String name) {
        List<String> values = headers.get(name);
        return values == null ? null : values.get(0);
    }

    public int getHttpStatusCode() {
        return response.getHttpStatusCode();
    }
}
