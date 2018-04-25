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

package org.lendingclub.http.breeze.client.impl.jaxrs;

import org.slf4j.Logger;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.SyncInvoker;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.lendingclub.http.breeze.client.BreezeHttpRequest;
import org.lendingclub.http.breeze.client.BreezeHttpResponse;
import org.lendingclub.http.breeze.client.BreezeHttpType;
import org.lendingclub.http.breeze.client.exception.BreezeHttpException;
import org.lendingclub.http.breeze.client.exception.BreezeHttpResponseException;
import org.lendingclub.http.breeze.client.filter.BreezeHttpRequestFilter;
import org.lendingclub.http.breeze.client.impl.AbstractBreezeHttpClient;
import org.lendingclub.http.breeze.client.payload.BreezeRequestForm;

/**
 * General JAX-RS client, using a supplied Client instance; defaults to
 * whatever ClientBuilder.newClient() provides.
 *
 * @author Raul Acevedo
 */
public class BreezeHttpJaxRsClient extends AbstractBreezeHttpClient {
    private final Client client;
    private final Class<?> errorResponseClass;

    public BreezeHttpJaxRsClient() {
        this(null, null, null, null);
    }

    public BreezeHttpJaxRsClient(
            Client client,
            List<BreezeHttpRequestFilter> requestFilters,
            Logger logger,
            Class<?> errorResponseClass
    ) {
        super(requestFilters, logger);
        this.client = client;
        this.errorResponseClass = errorResponseClass;
    }

    public BreezeHttpJaxRsClient(List<BreezeHttpRequestFilter> requestFilters, Logger logger) {
        super(requestFilters, logger);
        this.client = ClientBuilder.newClient();
        this.errorResponseClass = String.class;
    }

    public Client getClient() {
        return client;
    }

    @Override
    public <T> BreezeHttpResponse<T> execute(BreezeHttpRequest request, BreezeHttpType<T> genericType, Object payload)
            throws BreezeHttpException {
        return invoke(
                request,
                new GenericType<>(genericType == null ? Void.class : genericType.getType()),
                payload
        );
    }

    protected <T> BreezeHttpResponse<T> invoke(BreezeHttpRequest request, GenericType<T> genericType, Object payload) {
        long startTime = System.currentTimeMillis();
        Response response = null;

        try {
            logRequestStart(request);

            response = buildInvoker(request).method(request.getMethod().toString(), createEntity(payload));
            validateResponse(request, response);
            BreezeHttpResponse<T> breezeResponse = createResponse(genericType, response, response.getStatus());

            logRequestEnd(request, startTime);
            return breezeResponse;
        } catch (BreezeHttpException e) {
            logRequestException(request, startTime, e);
            throw e;
        } catch (Exception e) {
            logRequestException(request, startTime, e);
            throw new BreezeHttpException("error executing " + request, e);
        } finally {
            if (response != null) {
                response.close();
            }
        }
    }

    protected SyncInvoker buildInvoker(BreezeHttpRequest request) {
        WebTarget target = getClient()
                .target(request.getRootUrl())
                .path(request.getPath())
                .resolveTemplates(request.getPathVariables());

        // Add query string variables
        for (Map.Entry<String, List<Object>> queryVariable : request.getQueryVariables().entrySet()) {
            target = target.queryParam(queryVariable.getKey(), queryVariable.getValue().toArray());
        }

        // Add HTTP headers
        Invocation.Builder invoker = target.request(MediaType.APPLICATION_JSON_TYPE);
        for (Map.Entry<String, List<String>> entry : request.getHeaders().entrySet()) {
            for (String value : entry.getValue()) {
                invoker.header(entry.getKey(), value);
            }
        }

        return invoker;
    }

    protected Entity<?> createEntity(Object payload) {
        Entity<?> entity = null;

        if (payload instanceof InputStream) {
            entity = Entity.entity(payload, MediaType.APPLICATION_OCTET_STREAM_TYPE);
        } else if (payload instanceof BreezeRequestForm) {
            Form form = new Form();
            BreezeRequestForm breezeForm = (BreezeRequestForm) payload;
            breezeForm.params().forEach(form::param);
            entity = Entity.entity(form, MediaType.APPLICATION_FORM_URLENCODED_TYPE);
        } else if (payload != null) {
            entity = Entity.json(payload);
        }

        return entity;
    }

    protected void validateResponse(BreezeHttpRequest request, Response response) {
        int statusCode = response.getStatus();
        int statusFamily = statusCode / 100;
        if (statusFamily != 2) {
            String message = null;
            Object body = null;

            if ((statusFamily == 4 || statusFamily == 5) && response.hasEntity()) {
                try {
                    message = "service failed with "
                            + (statusFamily == 4 ? "client" : "server")
                            + " error invoking " + request;
                    body = response.readEntity(errorResponseClass);
                } catch (Exception e) {
                    logger.warn("error parsing response for " + request, e);
                }
            }

            if (message == null) {
                message = "service failed with unexpected error invoking " + request;
            }

            throw new BreezeHttpResponseException(
                    message,
                    request,
                    new BreezeHttpResponse<>(body, statusCode, headers(response))
            );
        }
    }

    protected <T> BreezeHttpResponse<T> createResponse(GenericType<T> genericType, Response response, int statusCode) {
        BreezeHttpResponse<T> breezeResponse;
        if (response.hasEntity() && genericType != null) {
            breezeResponse = new BreezeHttpResponse<>(response.readEntity(genericType), statusCode, headers(response));
        } else {
            breezeResponse = new BreezeHttpResponse<>(null, statusCode, headers(response));
        }
        return breezeResponse;
    }

    protected Map<String, List<String>> headers(Response response) {
        Map<String, List<String>> headers = new HashMap<>();

        if (response != null) {
            response.getStringHeaders().forEach(
                    (key, value) -> headers.put(key.toLowerCase(), new ArrayList<>(value))
            );
        }

        return headers;
    }
}
