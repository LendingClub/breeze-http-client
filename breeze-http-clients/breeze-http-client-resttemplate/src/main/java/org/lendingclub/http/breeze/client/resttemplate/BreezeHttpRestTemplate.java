package org.lendingclub.http.breeze.client.resttemplate;

import org.lendingclub.http.breeze.client.resttemplate.response.BreezeHttpRestTemplateRawResponse;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.converter.HttpMessageConverter;

import java.lang.reflect.Type;
import java.net.URI;
import java.util.List;

public interface BreezeHttpRestTemplate {
    BreezeHttpRestTemplateRawResponse breezeExecute(
            URI url,
            HttpMethod method,
            HttpEntity<?> requestEntity,
            Type type,
            boolean bufferResponse
    );

    ClientHttpRequestFactory getRequestFactory();

    void setRequestFactory(ClientHttpRequestFactory requestFactory);

    List<HttpMessageConverter<?>> getMessageConverters();
}
