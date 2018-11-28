package org.lendingclub.http.breeze.client.resttemplate;

import org.lendingclub.http.breeze.client.resttemplate.response.BreezeHttpRestTemplateRawResponse;
import org.lendingclub.http.breeze.request.BreezeHttpRequest;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.converter.HttpMessageConverter;

import java.net.URI;
import java.util.List;

public interface BreezeHttpRestTemplate {
    BreezeHttpRestTemplateRawResponse breezeExecute(
            URI url,
            HttpMethod method,
            HttpEntity<?> requestEntity,
            BreezeHttpRequest request
    );

    ClientHttpRequestFactory getRequestFactory();

    void setRequestFactory(ClientHttpRequestFactory requestFactory);

    List<HttpMessageConverter<?>> getMessageConverters();
}
