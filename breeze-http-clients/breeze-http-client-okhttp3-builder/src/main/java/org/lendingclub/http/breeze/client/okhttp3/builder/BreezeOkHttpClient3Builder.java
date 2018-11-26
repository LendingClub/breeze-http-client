package org.lendingclub.http.breeze.client.okhttp3.builder;

import okhttp3.Cache;
import okhttp3.ConnectionPool;
import okhttp3.OkHttpClient;
import org.lendingclub.http.breeze.builder.AbstractBreezeHttpClientBuilder;

import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.X509TrustManager;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.concurrent.TimeUnit;

public class BreezeOkHttpClient3Builder {
    protected OkHttpClient.Builder builder;
    protected Integer connectTimeout = null;
    protected Integer readTimeout = null;
    protected Integer writeTimeout;
    protected Boolean httpsAllowAllHosts = null;
    protected Proxy proxy;
    protected ConnectionPool connectionPool;
    protected SSLSocketFactory sslSocketFactory;
    protected X509TrustManager trustManager;
    protected Boolean followRedirects;
    protected Boolean followSslRedirects;
    protected Boolean retryOnConnectionFailure;
    protected Cache cache;
    protected Long pingInterval;

    public BreezeOkHttpClient3Builder okClient(OkHttpClient okClient) {
        this.builder = okClient.newBuilder();
         return this;
    }

    public BreezeOkHttpClient3Builder connectTimeout(Integer connectTimeout) {
        this.connectTimeout = connectTimeout;
         return this;
    }

    public BreezeOkHttpClient3Builder readTimeout(Integer readTimeout) {
        this.readTimeout = readTimeout;
         return this;
    }

    public BreezeOkHttpClient3Builder writeTimeout(Integer writeTimeout) {
        this.writeTimeout = writeTimeout;
        return this;
    }

    public BreezeOkHttpClient3Builder timeouts(Integer connectTimeout, Integer readTimeout, Integer writeTimeout) {
        this.connectTimeout = connectTimeout;
        this.readTimeout = readTimeout;
        this.writeTimeout = writeTimeout;
         return this;
    }

    public BreezeOkHttpClient3Builder httpsAllowAllHosts(boolean httpsAllowAllHosts) {
        this.httpsAllowAllHosts = httpsAllowAllHosts;
         return this;
    }

    public BreezeOkHttpClient3Builder proxy(Proxy proxy) {
        this.proxy = proxy;
         return this;
    }

    public BreezeOkHttpClient3Builder proxy(String hostname, Integer proxyPort, Proxy.Type type) {
        this.proxy = new Proxy(type, new InetSocketAddress(hostname, proxyPort));
         return this;
    }

    public BreezeOkHttpClient3Builder connectionPool(ConnectionPool connectionPool) {
        this.connectionPool = connectionPool;
         return this;
    }

    public BreezeOkHttpClient3Builder connectionPool(Integer maxIdleConnections, Long keepAliveDuration) {
        if (maxIdleConnections != null || keepAliveDuration != null) {
            this.connectionPool = new ConnectionPool(
                    maxIdleConnections == null ? 5 : maxIdleConnections,
                    keepAliveDuration == null ? 5000 : keepAliveDuration,
                    TimeUnit.MILLISECONDS
            );
        }
         return this;
    }

    public BreezeOkHttpClient3Builder sslSocketFactory(SSLSocketFactory sslSocketFactory, X509TrustManager trustManager) {
        this.sslSocketFactory = sslSocketFactory;
        this.trustManager = trustManager;
         return this;
    }

    public BreezeOkHttpClient3Builder followRedirects(Boolean followRedirects) {
        this.followRedirects = followRedirects;
         return this;
    }

    public BreezeOkHttpClient3Builder followSslRedirects(Boolean followSslRedirects) {
        this.followSslRedirects = followSslRedirects;
         return this;
    }

    public BreezeOkHttpClient3Builder retryOnConnectionFailure(Boolean retryOnConnectionFailure) {
        this.retryOnConnectionFailure = retryOnConnectionFailure;
         return this;
    }

    public BreezeOkHttpClient3Builder cache(Cache cache) {
        this.cache = cache;
         return this;
    }

    public BreezeOkHttpClient3Builder pingInterval(Long pingInterval) {
        this.pingInterval = pingInterval;
         return this;
    }

    public OkHttpClient build() {
        if (builder == null) {
            builder = new OkHttpClient.Builder();
        }

        if (connectTimeout != null) {
            builder.connectTimeout(connectTimeout, TimeUnit.MILLISECONDS);
        }

        if (readTimeout != null) {
            builder.readTimeout(readTimeout, TimeUnit.MILLISECONDS);
        }

        if (writeTimeout != null) {
            builder.writeTimeout(writeTimeout, TimeUnit.MILLISECONDS);
        }

        if (proxy != null) {
            builder.proxy(proxy);
        }

        if (httpsAllowAllHosts == Boolean.TRUE) {
            builder.hostnameVerifier(new AbstractBreezeHttpClientBuilder.NoopHostnameVerifier());
        }

        if (connectionPool != null) {
            builder.connectionPool(connectionPool);
        }

        if (sslSocketFactory != null &&  trustManager != null) {
            builder.sslSocketFactory(sslSocketFactory, trustManager);
        }

        if (followRedirects != null) {
            builder.followRedirects(followRedirects);
        }

        if (followSslRedirects != null) {
            builder.followSslRedirects(followSslRedirects);
        }

        if (retryOnConnectionFailure != null) {
            builder.retryOnConnectionFailure(retryOnConnectionFailure);
        }

        if (cache != null) {
            builder.cache(cache);
        }

        if (pingInterval != null) {
            builder.pingInterval(pingInterval, TimeUnit.MILLISECONDS);
        }

        return builder.build();
    }
}
