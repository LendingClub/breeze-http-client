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

package org.lendingclub.http.breeze.client.impl.resttemplate.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.net.ssl.SSLContext;

import org.lendingclub.http.breeze.client.BreezeHttpClient;
import org.lendingclub.http.breeze.client.decorator.BreezeHttpClientDecorator;
import org.lendingclub.http.breeze.client.filter.UserAgentRequestFilter;
import org.lendingclub.http.breeze.client.impl.resttemplate.builder.BreezeHttpRestTemplateClientBuilder;

/**
 * Configuration class to easily construct client instances.
 *
 * @author Raul Acevedo
 */
@Configuration
public class BreezeHttpRestTemplateClientConfig {
    @Autowired(required = false)
    @Qualifier(value = "breezeHttpClientRetryDecorator")
    private BreezeHttpClientDecorator breezeHttpClientRetryDecorator;

    @Autowired(required = false)
    @Qualifier(value = "breezeHttpClientSSLContext")
    private SSLContext sslContext;

    @Bean
    public BreezeHttpClient breezeHttpRestTemplateClient(
            @Value(value = "${BREEZE_HTTP_RESTTEMPLATE_CLIENT_CONNECT_TIMEOUT:10000}") int connectTimeout,
            @Value(value = "${BREEZE_HTTP_RESTTEMPLATE_CLIENT_READ_TIMEOUT:10000}") int readTimeout,
            @Value(value = "${BREEZE_HTTP_PROXY_HOST:}") String proxyHost,
            @Value(value = "${BREEZE_HTTP_PROXY_PORT:}") String proxyPort,
            @Value(value = "${BREEZE_HTTP_ENABLE_CLIENT_AUTH:true}") boolean enableClientAuth,
            @Value(value = "${BREEZE_HTTP_HTTPS_ALLOW_ALL_HOSTS:false}") boolean httpsAllowAllHosts,
            @Value(value = "${BREEZE_HTTP_RESTTEMPLATE_CLIENT_MAX_CONNECTIONS:40}") int maxConnections,
            @Value(value = "${BREEZE_HTTP_RESTTEMPLATE_CLIENT_MAX_CONNECTIONS_PER_ROUTE:40}") int maxConnectionsPerRoute,
            @Value(value = "${BREEZE_HTTP_RESTTEMPLATE_CLIENT_ERROR_RESPONSE_CLASS:java.lang.String}")
                    String errorResponseClass
    ) throws ClassNotFoundException {
        return new BreezeHttpRestTemplateClientBuilder()
                .withFilter(new UserAgentRequestFilter())
                .withDecorator(breezeHttpClientRetryDecorator)
                .withErrorResponseClass(Class.forName(errorResponseClass))
                .withTimeout(connectTimeout, readTimeout)
                .withProxy(proxyHost, proxyPort)
                .withSSLContext(enableClientAuth ? sslContext : null)
                .withHttpsAllowAllHosts(httpsAllowAllHosts)
                .withMaxConnections(maxConnections)
                .withMaxConnectionsPerRoute(maxConnectionsPerRoute)
                .build();
    }
}
