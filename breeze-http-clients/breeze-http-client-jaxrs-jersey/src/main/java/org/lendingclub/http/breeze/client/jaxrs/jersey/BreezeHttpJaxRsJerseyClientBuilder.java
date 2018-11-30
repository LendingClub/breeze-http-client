package org.lendingclub.http.breeze.client.jaxrs.jersey;

import org.lendingclub.http.breeze.BreezeHttp;
import org.lendingclub.http.breeze.builder.AbstractBreezeHttpClientBuilder;

import javax.ws.rs.client.Client;

public class BreezeHttpJaxRsJerseyClientBuilder
        extends AbstractBreezeHttpClientBuilder<BreezeHttpJaxRsJerseyClientBuilder> {
    private Client client;

    public BreezeHttpJaxRsJerseyClientBuilder() {
        this.me = this;
    }

    public BreezeHttpJaxRsJerseyClientBuilder client(Client client) {
        this.client = client;
        return this;
    }

    @Override
    public BreezeHttp build() {
        return decorate(new BreezeHttpJaxRsJerseyClient(
                client,
                requestLogger,
                filters,
                converters,
                errorHandler
        ));
    }
}
