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

package org.lendingclub.http.breeze.client;

import java.util.List;

import org.lendingclub.http.breeze.client.exception.BreezeHttpException;
import org.lendingclub.http.breeze.client.filter.BreezeHttpRequestFilter;

/**
 * Fluent REST API client interface.
 *
 * @author Raul Acevedo
 */
public interface BreezeHttpClient {
    /** Create new request with client's default rootUrl and remote service name. */
    BreezeHttpRequest request();

    /** Create new request to invoke the given rootUrl and no service name. */
    BreezeHttpRequest request(String rootUrl);

    /** Invoke GET request. */
    void get(BreezeHttpRequest request) throws BreezeHttpException;

    /** Invoke GET request, returning object of given response class. */
    <T> T get(BreezeHttpRequest request, Class<T> responseClass) throws BreezeHttpException;

    /** Invoke GET request, returning object of the given generic type. */
    <T> T get(BreezeHttpRequest request, BreezeHttpType<T> responseType) throws BreezeHttpException;

    /** Invoke POST with given payload. InputStream payloads are streamed. */
    void post(BreezeHttpRequest request, Object payload) throws BreezeHttpException;

    /** Invoke POST with given payload, returning object of given response class. InputStream payloads are streamed. */
    <T> T post(BreezeHttpRequest request, Class<T> responseType, Object payload) throws BreezeHttpException;

    /** Invoke POST with given payload, returning object of the given generic type. InputStream payloads are streamed. */
    <T> T post(BreezeHttpRequest request, BreezeHttpType<T> responseType, Object payload) throws BreezeHttpException;

    /** Invoke PUT with given payload. InputStream payloads are streamed. */
    void put(BreezeHttpRequest request, Object payload) throws BreezeHttpException;

    /** Invoke PUT with given payload, returning object of given response class. InputStream payloads are streamed. */
    <T> T put(BreezeHttpRequest request, Class<T> responseType, Object payload) throws BreezeHttpException;

    /** Invoke PUT with given payload, returning object of the given generic type. InputStream payloads are streamed. */
    <T> T put(BreezeHttpRequest request, BreezeHttpType<T> responseType, Object payload) throws BreezeHttpException;

    /** Invoke PATCH with given payload. InputStream payloads are streamed. */
    void patch(BreezeHttpRequest request, Object payload) throws BreezeHttpException;

    /** Invoke PATCH with given payload, returning object of given response class. InputStream payloads are streamed. */
    <T> T patch(BreezeHttpRequest request, Class<T> responseType, Object payload) throws BreezeHttpException;

    /** Invoke PATCH with given payload, returning object of the given generic type. InputStream payloads are streamed. */
    <T> T patch(BreezeHttpRequest request, BreezeHttpType<T> responseType, Object payload) throws BreezeHttpException;

    /** Execute an HTTP request based on its method, returning a full response of the given response class. */
    <T> BreezeHttpResponse<T> execute(BreezeHttpRequest request, Class<T> responseClass, Object payload)
            throws BreezeHttpException;

    /** Execute an HTTP request based on its method, returning a full response of the given response type. */
    <T> BreezeHttpResponse<T> execute(BreezeHttpRequest request, BreezeHttpType<T> responseType, Object payload)
            throws BreezeHttpException;

    /** Get the request filters configured for this client. */
    List<BreezeHttpRequestFilter> getRequestFilters();

    /** Create new client instance that defaults requests with the given rootUrl and remote service name. */
    BreezeHttpClient forService(String rootUrl, String remoteService);
}
