package org.lendingclub.http.breeze.client.resttemplate.builder;

import org.lendingclub.http.breeze.BreezeHttp;
import org.lendingclub.http.breeze.builder.AbstractBreezeHttpClientBuilder;
import org.lendingclub.http.breeze.client.resttemplate.BreezeHttpRestTemplate;
import org.lendingclub.http.breeze.client.resttemplate.BreezeHttpRestTemplateClient;
import org.lendingclub.http.breeze.client.resttemplate.BreezeRestTemplate;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

public class BreezeHttpRestTemplateClientBuilder
        extends AbstractBreezeHttpClientBuilder<BreezeHttpRestTemplateClientBuilder> {
    protected BreezeHttpRestTemplate breezeTemplate;
    protected ClientHttpRequestFactory requestFactory;

    public BreezeHttpRestTemplateClientBuilder() {
        this.me = this;
    }

    public BreezeHttpRestTemplateClientBuilder copyConfig(RestTemplate restTemplate) {
        this.breezeTemplate = new BreezeRestTemplate(restTemplate);
        return this;
    }

    public BreezeHttpRestTemplateClientBuilder breezeTemplate(BreezeHttpRestTemplate breezeTemplate) {
        this.breezeTemplate = breezeTemplate;
        return this;
    }

    public BreezeHttpRestTemplateClientBuilder requestFactory(ClientHttpRequestFactory requestFactory) {
        this.requestFactory = requestFactory;
        return this;
    }

    @Override
    public BreezeHttp build() {
        if (breezeTemplate == null) {
            breezeTemplate = new BreezeRestTemplate();
        }

        if (requestFactory != null) {
            breezeTemplate.setRequestFactory(requestFactory);
        }

        return decorate(new BreezeHttpRestTemplateClient(
                breezeTemplate,
                requestLogger,
                converters,
                filters,
                errorHandler
        ));
    }
}
