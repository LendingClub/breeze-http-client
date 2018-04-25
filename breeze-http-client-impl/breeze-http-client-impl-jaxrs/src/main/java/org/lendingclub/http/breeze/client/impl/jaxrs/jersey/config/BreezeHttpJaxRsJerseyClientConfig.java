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

package org.lendingclub.http.breeze.client.impl.jaxrs.jersey.config;

import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.ClientProperties;
import org.glassfish.jersey.client.HttpUrlConnectorProvider;
import org.glassfish.jersey.client.JerseyClient;
import org.glassfish.jersey.client.JerseyClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.fasterxml.jackson.jaxrs.json.JacksonJaxbJsonProvider;
import org.lendingclub.http.breeze.client.filter.UserAgentRequestFilter;
import org.lendingclub.http.breeze.client.impl.jaxrs.BreezeHttpJaxRsClient;

import static com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES;
import static java.util.Collections.singletonList;

/**
 * Easy way configure a JAX-RS Jersey client.
 *
 * @author Raul Acevedo
 */
@Configuration
public class BreezeHttpJaxRsJerseyClientConfig {
    @Bean
    public BreezeHttpJaxRsClient breezeHttpJaxRsJerseyClient(
            @Value(value = "${BREEZE_HTTP_JAXRS_JERSEY_CLIENT_CONNECT_TIMEOUT:10000}") int connectTimeout,
            @Value(value = "${BREEZE_HTTP_JAXRS_JERSEY_CLIENT_READ_TIMEOUT:10000}") int readTimeout,
            @Value(value = "${BREEZE_HTTP_JAXRS_JERSEY_CLIENT_ERROR_RESPONSE_CLASS:java.lang.String}")
                    String errorResponseClass
    ) throws ClassNotFoundException {
        // jackson sucks
        ClientConfig clientConfig = new ClientConfig(
                new JacksonJaxbJsonProvider().configure(FAIL_ON_UNKNOWN_PROPERTIES, false)
        );
        JerseyClient client = JerseyClientBuilder.createClient(clientConfig)
                .property(ClientProperties.CONNECT_TIMEOUT, connectTimeout)
                .property(ClientProperties.READ_TIMEOUT, readTimeout)
                .property(HttpUrlConnectorProvider.SET_METHOD_WORKAROUND, true);

        return new BreezeHttpJaxRsClient(
                client,
                singletonList(new UserAgentRequestFilter()),
                null,
                Class.forName(errorResponseClass)
        );
    }
}
