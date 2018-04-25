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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.HttpMessageConverterExtractor;
import org.springframework.web.client.ResponseExtractor;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.lendingclub.http.breeze.client.BreezeHttpRequest;
import org.lendingclub.http.breeze.client.BreezeHttpResponse;
import org.lendingclub.http.breeze.client.exception.BreezeHttpException;
import org.lendingclub.http.breeze.client.exception.BreezeHttpResponseException;

/**
 * Error handler for non-200 HTTP responses.
 *
 * @author Raul Acevedo
 */
public class BreeezeHttpRestTemplateErrorHandler implements ClientErrorHandler {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final ResponseExtractor<?> responseExtractor;

    public BreeezeHttpRestTemplateErrorHandler(Class<?> errorResponseClass) {
        this.responseExtractor = new HttpMessageConverterExtractor<>(
                errorResponseClass,
                new RestTemplate().getMessageConverters()
        );
    }

    @Override
    public boolean isErrorCode(int httpStatusCode) {
        return httpStatusCode / 100 != 2;
    }

    @Override
    public BreezeHttpException handleError(
            BreezeHttpRequest request,
            int httpStatusCode,
            ClientHttpResponse clientResponse
    ) {
        Object remoteErrorObject = null;
        String message = "external service failed with unexpected error";

        int statusFamily = httpStatusCode / 100;
        if (statusFamily == 4 || statusFamily == 5) {
            try {
                message = "external service failed with " + (statusFamily == 4 ? "client" : "server") + " error";
                remoteErrorObject = responseExtractor.extractData(clientResponse);
            } catch (Exception parseException) {
                logger.warn(parseException + " parsing error response");
            }
        }

        BreezeHttpResponse response = new BreezeHttpResponse<>(remoteErrorObject, httpStatusCode, headers(clientResponse));
        return new BreezeHttpResponseException(
                message + " invoking " + request,
                request,
                response);
    }

    @Override
    public BreezeHttpException handleError(BreezeHttpRequest request, Exception e) {
        return new BreezeHttpException("error invoking " + request, e);
    }

    protected Map<String, List<String>> headers(ClientHttpResponse response) {
        Map<String, List<String>> headers = new HashMap<>();

        if (response != null) {
            response.getHeaders().forEach(
                    (key, value) -> headers.put(key.toLowerCase(), new ArrayList<>(value))
            );
        }

        return headers;
    }
}
