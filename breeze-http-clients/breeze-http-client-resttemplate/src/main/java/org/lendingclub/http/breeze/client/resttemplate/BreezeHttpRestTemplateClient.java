package org.lendingclub.http.breeze.client.resttemplate;

import org.lendingclub.http.breeze.client.AbstractInvokingBreezeHttpClient;
import org.lendingclub.http.breeze.converter.BreezeHttpConverter;
import org.lendingclub.http.breeze.error.BreezeHttpErrorHandler;
import org.lendingclub.http.breeze.filter.BreezeHttpFilter;
import org.lendingclub.http.breeze.logging.BreezeHttpRequestLogger;
import org.lendingclub.http.breeze.request.BreezeHttpRequest;
import org.lendingclub.http.breeze.request.body.BreezeHttpForm;
import org.lendingclub.http.breeze.request.body.BreezeHttpMultipart;
import org.lendingclub.http.breeze.request.body.BreezeHttpMultipartPart;
import org.lendingclub.http.breeze.response.BreezeHttpRawResponse;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.PathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.client.OkHttp3ClientHttpRequestFactory;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.File;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class BreezeHttpRestTemplateClient extends AbstractInvokingBreezeHttpClient {
    protected final BreezeHttpRestTemplate breezeTemplate;

    public BreezeHttpRestTemplateClient() {
        this(null, null, null, null, null);
    }

    public BreezeHttpRestTemplateClient(
            BreezeHttpRestTemplate breezeTemplate,
            BreezeHttpRequestLogger requestLogger,
            Collection<BreezeHttpFilter> filters,
            Collection<BreezeHttpConverter> converters,
            BreezeHttpErrorHandler errorHandler
    ) {
        super(requestLogger, filters, converters, errorHandler);
        this.breezeTemplate = breezeTemplate != null ? breezeTemplate : new BreezeRestTemplate();
    }

    public BreezeHttpRestTemplate breezeTemplate() {
        return breezeTemplate;
    }

    @Override
    protected BreezeHttpRawResponse invoke(BreezeHttpRequest request) {
        return breezeTemplate.breezeExecute(
                buildURI(request),
                HttpMethod.valueOf(request.method().toString()),
                createRequestEntity(request),
                request
        );
    }

    protected URI buildURI(BreezeHttpRequest request) {
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(request.url()).path(request.path());

        if (!request.queryParameters().isEmpty()) {
            for (Map.Entry<String, List<Object>> queryVariable : request.queryParameters().entrySet()) {
                builder.queryParam(queryVariable.getKey(), queryVariable.getValue().toArray());
            }
        }

        return builder.buildAndExpand(request.pathVariables()).encode().toUri();
    }

    protected HttpEntity<?> createRequestEntity(BreezeHttpRequest request) {
        // OkHttp3 magically handles gzip already; if you explicitly set the
        // header, you have to parse the byte[] yourself.
        if (breezeTemplate.getRequestFactory() instanceof OkHttp3ClientHttpRequestFactory
                && "gzip".equals(request.header("Accept-Encoding"))
                && request.returnType() != byte[].class) {
            request.header("Accept-Encoding", null);
        }

        HttpHeaders headers = new HttpHeaders();
        headers.putAll(request.headers());

        Object body = request.body();

        if (body instanceof BreezeHttpForm) {
            BreezeHttpForm form = (BreezeHttpForm) body;
            MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
            form.params().forEach(map::add);
            return new HttpEntity<>(map, headers);
        }

        if (body instanceof BreezeHttpMultipart) {
            BreezeHttpMultipart multipart = (BreezeHttpMultipart) body;
            MultiValueMap<String, Object> multimap = new LinkedMultiValueMap<>();
            for (BreezeHttpMultipartPart part : multipart.parts()) {
                HttpHeaders partHeaders = new HttpHeaders();
                partHeaders.setContentType(MediaType.parseMediaType(part.contentType()));
                HttpEntity<?> entity = createPassthroughEntity(part.body(), partHeaders);
                multimap.add(part.name(), entity);
            }
            return new HttpEntity<>(multimap, headers);
        }

        return createPassthroughEntity(body, headers);
    }

    protected HttpEntity<?> createPassthroughEntity(Object body, HttpHeaders headers) {
        if (body instanceof HttpEntity) {
            return (HttpEntity) body;
        }

        if (body instanceof Resource) {
            return new HttpEntity<>((Resource) body, headers);
        }

        if (body instanceof File) {
            return new HttpEntity<>(new FileSystemResource((File) body), headers);
        }

        if (body instanceof Path) {
            return new HttpEntity<>(new PathResource((Path) body), headers);
        }

        if (body instanceof byte[]) {
            return new HttpEntity<>(new ByteArrayResource((byte[]) body), headers);
        }

        if (body instanceof InputStream) {
            return new HttpEntity<>(new InputStreamResource((InputStream) body), headers);
        }

        return new HttpEntity<>(body, headers);
    }
}
