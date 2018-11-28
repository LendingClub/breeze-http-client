package org.lendingclub.http.breeze.client.resttemplate.response;

import org.lendingclub.http.breeze.converter.BreezeHttpConverter;
import org.lendingclub.http.breeze.exception.BreezeHttpException;
import org.lendingclub.http.breeze.response.BreezeHttpRawResponse;
import org.lendingclub.http.breeze.response.BreezeHttpResponseInputStream;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.client.HttpMessageConverterExtractor;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import static org.lendingclub.http.breeze.util.BreezeHttpUtil.cast;
import static org.lendingclub.http.breeze.util.BreezeHttpUtil.readBytes;
import static org.lendingclub.http.breeze.util.BreezeHttpUtil.readString;

public class BreezeHttpRestTemplateRawResponse extends BreezeHttpRawResponse {
    private final ClientHttpResponse clientResponse;
    private final List<HttpMessageConverter<?>> messageConverters = new ArrayList<>();

    public BreezeHttpRestTemplateRawResponse(
            List<BreezeHttpConverter> converters,
            ClientHttpResponse clientResponse,
            List<HttpMessageConverter<?>> messageConverters
    ) {
        super(converters);
        this.clientResponse = clientResponse;
        this.messageConverters.addAll(messageConverters);
        clientResponse.getHeaders().forEach(this::header);

        try {
            this.httpStatus = clientResponse.getRawStatusCode();
        } catch (IOException e) {
            throw new BreezeHttpException(e);
        }
    }

    @Override
    public InputStream body() {
        try {
            return new BreezeHttpResponseInputStream(clientResponse, clientResponse.getBody());
        } catch (IOException e) {
            throw new BreezeHttpException(e);
        }
    }

    @Override
    public String string() {
        try {
            HttpHeaders headers = clientResponse.getHeaders();
            MediaType contentType = headers.getContentType();
            String charset = contentType != null ? contentType.getCharset().name() : "UTF-8";
            return readString(clientResponse.getBody(), charset);
        } catch (IOException e) {
            throw new BreezeHttpException(e);
        }
    }

    @Override
    public byte[] bytes() {
        try {
            return readBytes(clientResponse.getBody());
        } catch (IOException e) {
            throw new BreezeHttpException(e);
        }
    }

    @Override
    public <T> T convertBody(Type type) {
        try {
            if (type == null || type == Void.class) {
                return null;
            } else if (type == InputStream.class) {
                return cast(new BreezeHttpResponseInputStream(clientResponse, clientResponse.getBody()));
            } else {
                return new HttpMessageConverterExtractor<T>(type, messageConverters).extractData(clientResponse);
            }
        } catch (IOException e) {
            throw new BreezeHttpException(e);
        }
    }

    @Override
    public void close() {
        clientResponse.close();
    }
}
