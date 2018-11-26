package org.lendingclub.http.breeze.client.resttemplate.builder;

import org.apache.http.HttpHost;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.conn.ssl.DefaultHostnameVerifier;
import org.apache.http.impl.client.HttpClientBuilder;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;

import javax.net.ssl.SSLContext;

import static org.lendingclub.http.breeze.builder.AbstractBreezeHttpClientBuilder.NoopHostnameVerifier;

public class BreezeHttpComponentsRequestFactoryBuilder {
    protected Integer connectTimeout = null;
    protected Integer socketTimeout = null;
    protected HttpHost proxyHost;
    protected Integer maxConnections = null;
    protected Integer maxConnectionsPerRoute = null;
    protected SSLContext sslContext;
    protected Boolean httpsAllowAllHosts = null;
    protected Boolean poolStatefulSslConnections = null;

    public BreezeHttpComponentsRequestFactoryBuilder connectTimeout(Integer connectTimeout) {
        this.connectTimeout = connectTimeout;
        return this;
    }

    public BreezeHttpComponentsRequestFactoryBuilder socketTimeout(Integer socketTimeout) {
        this.socketTimeout = socketTimeout;
        return this;
    }

    public BreezeHttpComponentsRequestFactoryBuilder timeouts(Integer connectTimeout, Integer socketTimeout) {
        this.connectTimeout = connectTimeout;
        this.socketTimeout = socketTimeout;
        return this;
    }

    public BreezeHttpComponentsRequestFactoryBuilder proxyHost(HttpHost proxyHost) {
        this.proxyHost = proxyHost;
        return this;
    }

    public BreezeHttpComponentsRequestFactoryBuilder proxy(String proxyHost, Integer proxyPort, String type) {
        if (proxyHost != null && proxyPort != null && type != null) {
            this.proxyHost = new HttpHost(proxyHost, proxyPort, type);
        }
        return this;
    }

    public BreezeHttpComponentsRequestFactoryBuilder maxConnections(Integer maxConnections) {
        this.maxConnections = maxConnections;
        return this;
    }

    public BreezeHttpComponentsRequestFactoryBuilder maxConnectionsPerRoute(Integer maxConnectionsPerRoute) {
        this.maxConnectionsPerRoute = maxConnectionsPerRoute;
        return this;
    }

    public BreezeHttpComponentsRequestFactoryBuilder sslContext(SSLContext sslContext) {
        this.sslContext = sslContext;
        return this;
    }

    public BreezeHttpComponentsRequestFactoryBuilder httpsAllowAllHosts(Boolean httpsAllowAllHosts) {
        this.httpsAllowAllHosts = httpsAllowAllHosts;
        return this;
    }

    public BreezeHttpComponentsRequestFactoryBuilder poolStatefulSslConnections(Boolean poolStatefulSslConnections) {
        this.poolStatefulSslConnections = poolStatefulSslConnections;
        return this;
    }

    public HttpComponentsClientHttpRequestFactory build() {
        HttpClientBuilder builder = HttpClientBuilder.create();

        builder.setMaxConnTotal(maxConnections == null ? 40 : maxConnections);
        builder.setMaxConnPerRoute(maxConnectionsPerRoute == null ? 40 : maxConnectionsPerRoute);

        if (connectTimeout != null || socketTimeout != null) {
            RequestConfig.Builder requestConfigBuilder = RequestConfig.custom();
            if (connectTimeout != null) {
                requestConfigBuilder.setConnectTimeout(connectTimeout);
            }
            if (socketTimeout != null) {
                requestConfigBuilder.setSocketTimeout(socketTimeout);
            }
            builder.setDefaultRequestConfig(requestConfigBuilder.build());
        }

        if (sslContext != null) {
            builder.setSSLContext(sslContext);
        }

        if (httpsAllowAllHosts != null) {
            builder.setSSLHostnameVerifier(
                    httpsAllowAllHosts ? new NoopHostnameVerifier() : new DefaultHostnameVerifier()
            );
        }

        if (proxyHost != null) {
            builder.setProxy(proxyHost);
        }

        if (poolStatefulSslConnections != Boolean.FALSE) {
            // The DefaultUserTokenHandler will return the "principal" which authenticated the
            // connection; for LC internal APIs this is our SSL client certificate. To pool
            // SSL connections, we pretend there is no such principal by always returning null.
            // See https://hc.apache.org/httpcomponents-client-ga/tutorial/html/advanced.html#d5e945.
            builder.setUserTokenHandler(context -> null);
        }

        return new HttpComponentsClientHttpRequestFactory(builder.build());
    }
}
