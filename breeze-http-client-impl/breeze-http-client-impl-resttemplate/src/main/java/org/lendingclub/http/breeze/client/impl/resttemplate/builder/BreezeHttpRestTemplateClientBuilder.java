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

package org.lendingclub.http.breeze.client.impl.resttemplate.builder;

import org.apache.http.HttpHost;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.conn.ssl.DefaultHostnameVerifier;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.impl.client.HttpClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import javax.net.ssl.SSLContext;
import java.util.ArrayList;
import java.util.List;

import org.lendingclub.http.breeze.client.BreezeHttpClient;
import org.lendingclub.http.breeze.client.decorator.BreezeHttpClientDecorator;
import org.lendingclub.http.breeze.client.filter.BreezeHttpRequestFilter;
import org.lendingclub.http.breeze.client.impl.jackson.BreezeObjectMapper;
import org.lendingclub.http.breeze.client.impl.resttemplate.BreezeHttpRestTemplateClient;
import org.lendingclub.http.breeze.client.impl.resttemplate.error.BreeezeHttpRestTemplateErrorHandler;
import org.lendingclub.http.breeze.client.impl.resttemplate.error.ClientErrorHandler;

import static java.lang.Boolean.FALSE;
import static java.util.Arrays.asList;

/**
 * Builder for the RestTemplate client.
 *
 * @author Raul Acevedo
 */
public class BreezeHttpRestTemplateClientBuilder {
    private final List<BreezeHttpRequestFilter> filters = new ArrayList<>();
    private final List<BreezeHttpClientDecorator> decorators = new ArrayList<>();
    private RestTemplate restTemplate = new RestTemplate();
    private Logger logger = LoggerFactory.getLogger(BreezeHttpRestTemplateClient.class);
    private ClientErrorHandler errorHandler = null;
    private boolean useBreezeObjectMapperSettings = true;
    private ClientHttpRequestFactory requestFactory = null;
    private Integer connectTimeout = null;
    private Integer readTimeout = null;
    private String proxyHost = null;
    private String proxyPort = null;
    private SSLContext sslContext = null;
    private Integer maxConnections = null;
    private Integer maxConnectionsPerRoute = null;
    private Boolean httpsAllowAllHosts = null;
    private Boolean poolStatefulSslConnections = null;

    /**
     * Use the given RestTemplate instance.
     *
     * Its ResponseErrorHandler will be REPLACED by our own ClientErrorHandler.
     */
    public BreezeHttpRestTemplateClientBuilder withRestTemplate(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
        return this;
    }

    public BreezeHttpRestTemplateClientBuilder withMessageConverters(List<HttpMessageConverter<?>> messageConverters) {
        this.restTemplate.setMessageConverters(messageConverters);
        return this;
    }

    public BreezeHttpRestTemplateClientBuilder withInterceptors(List<ClientHttpRequestInterceptor> interceptors) {
        this.restTemplate.setInterceptors(interceptors);
        return this;
    }

    public BreezeHttpRestTemplateClientBuilder withFilter(BreezeHttpRequestFilter filter) {
        this.filters.add(filter);
        return this;
    }

    public BreezeHttpRestTemplateClientBuilder withDecorator(BreezeHttpClientDecorator decorator) {
        this.decorators.add(decorator);
        return this;
    }

    public BreezeHttpRestTemplateClientBuilder withDecorators(BreezeHttpClientDecorator... decorators) {
        return withDecorators(asList(decorators));
    }

    public BreezeHttpRestTemplateClientBuilder withDecorators(List<BreezeHttpClientDecorator> decorators) {
        this.decorators.addAll(decorators);
        return this;
    }

    public BreezeHttpRestTemplateClientBuilder withLogger(Logger logger) {
        this.logger = logger;
        return this;
    }

    public BreezeHttpRestTemplateClientBuilder withRequestFactory(ClientHttpRequestFactory requestFactory) {
        this.requestFactory = requestFactory;
        return this;
    }

    public BreezeHttpRestTemplateClientBuilder withTimeout(int connectTimeout, int readTimeout) {
        this.connectTimeout = connectTimeout;
        this.readTimeout = readTimeout;
        return this;
    }

    /** Use the given proxy. Ignored if either values is null or an empty string. */
    public BreezeHttpRestTemplateClientBuilder withProxy(String proxyHost, String proxyPort) {
        this.proxyHost = proxyHost;
        this.proxyPort = proxyPort;
        return this;
    }

    public BreezeHttpRestTemplateClientBuilder withSSLContext(SSLContext sslContext) {
        this.sslContext = sslContext;
        return this;
    }

    public BreezeHttpRestTemplateClientBuilder withHttpsAllowAllHosts(boolean httpsAllowAllHosts) {
        this.httpsAllowAllHosts = httpsAllowAllHosts;
        return this;
    }

    public BreezeHttpRestTemplateClientBuilder withMaxConnections(Integer maxConnections) {
        this.maxConnections = maxConnections;
        return this;
    }

    public BreezeHttpRestTemplateClientBuilder withMaxConnectionsPerRoute(Integer maxConnectionsPerRoute) {
        this.maxConnectionsPerRoute = maxConnectionsPerRoute;
        return this;
    }

    /**
     * By default, all SSL connections will be shared. If you don't want
     * threads sharing an authenticated SSL connection with other threads,
     * set this to false. By "authentication" we mean client certificates,
     * NTLM, or any type of stateful HTTPS connection. Typically any thread
     * can reuse a connection authenticated by any other thread in the same
     * application, so this is not an issue.
     *
     * Note that non-stateful SSL connections are always shared, regardless
     * of what poolStatefulSslConnections is set to.
     *
     * @see <a href="https://hc.apache.org/httpcomponents-client-ga/tutorial/html/advanced.html#d5e945">Persistent
     * stateful connections</a> in the Apache HttpComponents documentation.
     */
    public BreezeHttpRestTemplateClientBuilder withPoolStatefulSslConnections(Boolean poolStatefulSslConnections) {
        this.poolStatefulSslConnections = poolStatefulSslConnections;
        return this;
    }

    public BreezeHttpRestTemplateClientBuilder withErrorHandler(ClientErrorHandler errorHandler) {
        this.errorHandler = errorHandler;
        return this;
    }

    public BreezeHttpRestTemplateClientBuilder withErrorResponseClass(Class<?> errorResponseClass) {
        if (errorHandler != null) {
            throw new IllegalStateException("cannot specify both errorHandler and external API errorResponseCode");
        }
        this.errorHandler = new BreeezeHttpRestTemplateErrorHandler(errorResponseClass);
        return this;
    }

    /**
     * Use sane Jackson defaults; see BreezeObjectMapper.
     */
    public BreezeHttpRestTemplateClientBuilder useBreezeObjectMapperSettings(boolean useBreezeObjectMapperSettings) {
        this.useBreezeObjectMapperSettings = useBreezeObjectMapperSettings;
        return this;
    }

    public BreezeHttpClient build() {
        // Setup the RestTemplate request factory: timeouts, proxy and connection pooling
        setupRequestFactory();

        // Fix ObjectCrapper so it's not retarded
        setBreezeObjectMapperSettings();

        // Create the client with its decorators, filters, and other bells and whistles
        return createClient();
    }

    private void setupRequestFactory() {
        if (requestFactory != null) {
            if (connectTimeout != null
                    || readTimeout != null
                    || proxyHost != null
                    || proxyPort != null
                    || sslContext != null
                    || httpsAllowAllHosts != null) {
                throw new IllegalStateException("cannot use requestFactory with timeouts, proxy, or ssl config");
            }
            restTemplate.setRequestFactory(requestFactory);
        } else {
            HttpClientBuilder builder = HttpClientBuilder.create();

            builder.setMaxConnTotal(maxConnections == null ? 40 : maxConnections);
            builder.setMaxConnPerRoute(maxConnectionsPerRoute == null ? 40 : maxConnectionsPerRoute);

            if (connectTimeout != null) {
                RequestConfig requestConfig = RequestConfig.custom()
                        .setConnectTimeout(connectTimeout)
                        .setSocketTimeout(readTimeout)
                        .build();
                builder.setDefaultRequestConfig(requestConfig);
            }

            if (sslContext != null) {
                builder.setSSLContext(sslContext);
            }

            if (httpsAllowAllHosts != null) {
                builder.setSSLHostnameVerifier(
                        httpsAllowAllHosts ? NoopHostnameVerifier.INSTANCE : new DefaultHostnameVerifier()
                );
            }

            if (proxyHost != null && proxyHost.length() > 0 && proxyPort != null && proxyPort.length() > 0) {
                builder.setProxy(new HttpHost(proxyHost, Integer.parseInt(proxyPort), "http"));
            }

            if (poolStatefulSslConnections != FALSE) {
                // The DefaultUserTokenHandler will return the "principal" which authenticated the
                // connection. To pool SSL connections, we pretend there is no such principal by
                // always returning null.
                // See https://hc.apache.org/httpcomponents-client-ga/tutorial/html/advanced.html#d5e945.
                builder.setUserTokenHandler(context -> null);
            }

            restTemplate.setRequestFactory(new HttpComponentsClientHttpRequestFactory(builder.build()));
        }
    }

    private void setBreezeObjectMapperSettings() {
        if (useBreezeObjectMapperSettings) {
            restTemplate.getMessageConverters().stream()
                    .filter(converter -> converter instanceof MappingJackson2HttpMessageConverter)
                    .findFirst()
                    .ifPresent(converter ->
                            BreezeObjectMapper.configure(((MappingJackson2HttpMessageConverter) converter).getObjectMapper())
                    );
        }
    }

    private BreezeHttpClient createClient() {
        BreezeHttpClient client = new BreezeHttpRestTemplateClient(restTemplate, errorHandler, filters, logger);

        // Add decorators
        for (BreezeHttpClientDecorator decorator : decorators) {
            if (decorator != null) {
                client = decorator.decorate(client);
            }
        }

        return client;
    }
}
