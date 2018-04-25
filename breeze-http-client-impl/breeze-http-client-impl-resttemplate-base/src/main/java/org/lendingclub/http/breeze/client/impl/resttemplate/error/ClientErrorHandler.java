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

package org.lendingclub.http.breeze.client.impl.resttemplate.error;

import org.springframework.http.client.ClientHttpResponse;

import org.lendingclub.http.breeze.client.BreezeHttpRequest;
import org.lendingclub.http.breeze.client.exception.BreezeHttpException;

/**
 * Error handler class for all exceptions encountered invoking a request.
 *
 * @author Raul Acevedo
 */
public interface ClientErrorHandler {
    /**
     * Determines if a response HTTP status code is an error or not. If true,
     * handleError will be invoked with the RestTemplate ClientHttpResponse.
     */
    boolean isErrorCode(int httpStatusCode);

    /**
     * Invoked if there was an response from the server, based on the result
     * of calling isErrorCode().
     *
     * @param request original request, after filters, immediately before invocation
     * @param httpStatusCode HTTP status code
     * @param response RestTemplate response object
     * @return exception to throw; ideally has recoverability assigned, if possible
     */
    BreezeHttpException handleError(
            BreezeHttpRequest request,
            int httpStatusCode,
            ClientHttpResponse response
    );

    /**
     * Invoked if there was either a network error connecting to the server,
     * or any other unexpected error processing the request. Errors in this
     * category do not involve a server response (no HTTP status or body).
     *
     * @param request original request, after filters, immediately before invocation
     * @param e exception to handle
     * @return exception to throw; ideally has recoverability assigned, if possible
     */
    BreezeHttpException handleError(BreezeHttpRequest request, Exception e);
}
