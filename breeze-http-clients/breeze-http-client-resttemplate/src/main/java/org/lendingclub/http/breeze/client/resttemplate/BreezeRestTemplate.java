package org.lendingclub.http.breeze.client.resttemplate;

import org.lendingclub.http.breeze.client.resttemplate.response.BreezeHttpRestTemplateRawResponse;
import org.lendingclub.http.breeze.exception.BreezeHttpException;
import org.lendingclub.http.breeze.request.BreezeHttpRequest;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpRequest;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
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
            BreezeHttpRequest request
    ) {
        try {
            ClientHttpRequest httpRequest;
            if (request.bufferResponse()) {
                // Doesn't work if requestFactory has bufferRequestBody=false
                httpRequest = bufferingRequestFactory.createRequest(url, method);
            } else {
                httpRequest = createRequest(url, method);
            }

            httpEntityCallback(requestEntity, request.conversionType()).doWithRequest(httpRequest);
            return new BreezeHttpRestTemplateRawResponse(request, httpRequest.execute(), getMessageConverters());
        } catch (IOException e) {
            throw new BreezeHttpException(e);
        }
    }
}
