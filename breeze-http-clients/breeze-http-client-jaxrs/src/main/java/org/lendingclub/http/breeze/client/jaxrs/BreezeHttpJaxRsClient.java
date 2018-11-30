package org.lendingclub.http.breeze.client.jaxrs;

import org.lendingclub.http.breeze.client.AbstractInvokingBreezeHttpClient;
import org.lendingclub.http.breeze.client.jaxrs.response.BreezeHttpJaxRsRawResponse;
import org.lendingclub.http.breeze.converter.BreezeHttpConverter;
import org.lendingclub.http.breeze.error.BreezeHttpErrorHandler;
import org.lendingclub.http.breeze.filter.BreezeHttpFilter;
import org.lendingclub.http.breeze.logging.BreezeHttpRequestLogger;
import org.lendingclub.http.breeze.request.BreezeHttpRequest;
import org.lendingclub.http.breeze.request.body.BreezeHttpForm;
import org.lendingclub.http.breeze.request.body.BreezeHttpMultipart;
import org.lendingclub.http.breeze.response.BreezeHttpRawResponse;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.SyncInvoker;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.InputStream;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public abstract class BreezeHttpJaxRsClient extends AbstractInvokingBreezeHttpClient {
    public BreezeHttpJaxRsClient() {
        this(null, null, null, null);
    }

    public BreezeHttpJaxRsClient(
            BreezeHttpRequestLogger requestLogger,
            Collection<BreezeHttpFilter> filters,
            Collection<BreezeHttpConverter> converters,
            BreezeHttpErrorHandler errorHandler
    ) {
        super(requestLogger, filters, converters, errorHandler);
    }

    protected abstract Client client();

    protected abstract Entity<?> createMultipartEntity(BreezeHttpRequest request, BreezeHttpMultipart multipart);

    @Override
    protected BreezeHttpRawResponse invoke(BreezeHttpRequest request) {
        SyncInvoker invoker = buildInvoker(request);
        Response response = invoker.method(request.method().toString(), createEntity(request));
        if (request.bufferResponse()) {
            response.bufferEntity();
        }
        return new BreezeHttpJaxRsRawResponse(request, response);
    }

    protected SyncInvoker buildInvoker(BreezeHttpRequest request) {
        // Create base request target
        WebTarget target = client()
                .target(request.url())
                .path(request.path())
                .resolveTemplates(request.pathVariables());

        // Add query string variables
        for (Map.Entry<String, List<Object>> queryVariable : request.queryParameters().entrySet()) {
            target = target.queryParam(queryVariable.getKey(), queryVariable.getValue().toArray());
        }

        // Add HTTP headers
        Invocation.Builder invoker = target.request();
        for (Map.Entry<String, List<String>> entry : request.headers().entrySet()) {
            for (String value : entry.getValue()) {
                invoker.header(entry.getKey(), value);
            }
        }

        return invoker;
    }

    protected Entity<?> createEntity(BreezeHttpRequest request) {
        Object body = request.body();
        if (body == null) {
            return null;
        }

        if (body instanceof Entity) {
            return (Entity<?>) body;
        }

        if (body instanceof InputStream) {
            return Entity.entity(body, MediaType.APPLICATION_OCTET_STREAM_TYPE);
        }

        if (body instanceof BreezeHttpForm) {
            Form form = new Form();
            BreezeHttpForm breezeForm = (BreezeHttpForm) body;
            breezeForm.params().forEach(form::param);
            return Entity.entity(form, MediaType.APPLICATION_FORM_URLENCODED_TYPE);
        }

        if (body instanceof BreezeHttpMultipart) {
            return createMultipartEntity(request, (BreezeHttpMultipart) body);
        }

        return Entity.entity(body, MediaType.valueOf(request.contentType()));
    }
}
