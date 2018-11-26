package org.lendingclub.http.breeze.client.resttemplate.builder;

import org.springframework.http.client.SimpleClientHttpRequestFactory;

import java.net.InetSocketAddress;
import java.net.Proxy;

public class BreezeHttpSimpleRequestFactoryBuilder {
    protected Integer connectTimeout = null;
    protected Integer readTimeout = null;
    protected Proxy proxy;
    protected Boolean bufferRequestBody;
    protected Integer chunkSize;
    protected Boolean outputStreaming;

    public BreezeHttpSimpleRequestFactoryBuilder connectTimeout(Integer connectTimeout) {
        this.connectTimeout = connectTimeout;
        return this;
    }

    public BreezeHttpSimpleRequestFactoryBuilder readTimeout(Integer readTimeout) {
        this.readTimeout = readTimeout;
        return this;
    }

    public BreezeHttpSimpleRequestFactoryBuilder timeout(Integer connectTimeout, Integer readTimeout) {
        this.connectTimeout = connectTimeout;
        this.readTimeout = readTimeout;
        return this;
    }

    public BreezeHttpSimpleRequestFactoryBuilder proxy(Proxy proxy) {
        this.proxy = proxy;
        return this;
    }

    public BreezeHttpSimpleRequestFactoryBuilder proxy(String hostname, Integer proxyPort, Proxy.Type type) {
        this.proxy = new Proxy(type, new InetSocketAddress(hostname, proxyPort));
        return this;
    }

    public BreezeHttpSimpleRequestFactoryBuilder bufferRequestBody(Boolean bufferRequestBody) {
        this.bufferRequestBody = bufferRequestBody;
        return this;
    }

    public BreezeHttpSimpleRequestFactoryBuilder chunkSize(Integer chunkSize) {
        this.chunkSize = chunkSize;
        return this;
    }

    public BreezeHttpSimpleRequestFactoryBuilder outputStreaming(Boolean outputStreaming) {
        this.outputStreaming = outputStreaming;
        return this;
    }

    public SimpleClientHttpRequestFactory build() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();

        if (connectTimeout != null) {
            factory.setConnectTimeout(connectTimeout);
        }

        if (readTimeout != null) {
            factory.setReadTimeout(readTimeout);
        }

        if (proxy != null) {
            factory.setProxy(proxy);
        }

        if (bufferRequestBody != null) {
            factory.setBufferRequestBody(bufferRequestBody);
        }

        if (chunkSize != null) {
            factory.setChunkSize(chunkSize);
        }

        if (outputStreaming != null) {
            factory.setOutputStreaming(outputStreaming);
        }

        return factory;
    }
}
