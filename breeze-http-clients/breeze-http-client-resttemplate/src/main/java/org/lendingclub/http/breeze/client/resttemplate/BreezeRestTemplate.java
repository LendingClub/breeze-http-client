package org.lendingclub.http.breeze.client.resttemplate;

import org.lendingclub.http.breeze.client.resttemplate.response.BreezeHttpRestTemplateRawResponse;
import org.lendingclub.http.breeze.exception.BreezeHttpIOException;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpRequest;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URI;

public class BreezeRestTemplate extends RestTemplate implements BreezeHttpRestTemplate {
    private BufferingClientHttpRequestFactory bufferingRequestFactory;

    public BreezeRestTemplate() {
        this(new RestTemplate());
    }

    public BreezeRestTemplate(RestTemplate source) {
        setMessageConverters(source.getMessageConverters());
        setUriTemplateHandler(source.getUriTemplateHandler());
        setInterceptors(source.getInterceptors());
        setRequestFactory(source.getRequestFactory());
        setErrorHandler(source.getErrorHandler()); // ignored!
    }

    @Override
    public void setRequestFactory(ClientHttpRequestFactory requestFactory) {
        if (requestFactory instanceof BufferingClientHttpRequestFactory) {
            this.bufferingRequestFactory = (BufferingClientHttpRequestFactory) requestFactory;
        } else {
            this.bufferingRequestFactory = new BufferingClientHttpRequestFactory(requestFactory);
        }
        super.setRequestFactory(requestFactory);
    }

    @Override
    public BreezeHttpRestTemplateRawResponse breezeExecute(
            URI url,
            HttpMethod method,
            HttpEntity<?> requestEntity,
            Type conversionType,
            boolean bufferResponse
    ) {
        try {
            ClientHttpRequest request;
            if (bufferResponse) {
                // Doesn't work if requestFactory has bufferRequestBody=false
                request = bufferingRequestFactory.createRequest(url, method);
            } else {
                request = createRequest(url, method);
            }

            httpEntityCallback(requestEntity, conversionType).doWithRequest(request);
            return new BreezeHttpRestTemplateRawResponse(conversionType, request.execute(), getMessageConverters());
        } catch (IOException | ResourceAccessException e) {
            throw new BreezeHttpIOException(e);
        }
    }
}
