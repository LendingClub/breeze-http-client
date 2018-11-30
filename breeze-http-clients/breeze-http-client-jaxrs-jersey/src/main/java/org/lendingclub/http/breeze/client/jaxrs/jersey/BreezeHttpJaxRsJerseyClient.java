package org.lendingclub.http.breeze.client.jaxrs.jersey;

import org.glassfish.jersey.client.JerseyClientBuilder;
import org.glassfish.jersey.media.multipart.FormDataBodyPart;
import org.glassfish.jersey.media.multipart.MultiPart;
import org.glassfish.jersey.media.multipart.file.FileDataBodyPart;
import org.glassfish.jersey.media.multipart.file.StreamDataBodyPart;
import org.lendingclub.http.breeze.client.jaxrs.BreezeHttpJaxRsClient;
import org.lendingclub.http.breeze.converter.BreezeHttpConverter;
import org.lendingclub.http.breeze.error.BreezeHttpErrorHandler;
import org.lendingclub.http.breeze.filter.BreezeHttpFilter;
import org.lendingclub.http.breeze.logging.BreezeHttpRequestLogger;
import org.lendingclub.http.breeze.request.BreezeHttpRequest;
import org.lendingclub.http.breeze.request.body.BreezeHttpMultipart;
import org.lendingclub.http.breeze.request.body.BreezeHttpMultipartPart;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import java.io.File;
import java.io.InputStream;
import java.util.Collection;

public class BreezeHttpJaxRsJerseyClient extends BreezeHttpJaxRsClient {
    private final Client client;

    public BreezeHttpJaxRsJerseyClient() {
        this(null, null, null, null, null);
    }

    public BreezeHttpJaxRsJerseyClient(
            Client client,
            BreezeHttpRequestLogger requestLogger,
            Collection<BreezeHttpFilter> filters,
            Collection<BreezeHttpConverter> converters,
            BreezeHttpErrorHandler errorHandler
    ) {
        super(requestLogger, filters, converters, errorHandler);
        this.client = client == null ? JerseyClientBuilder.createClient() : client;
    }

    @Override
    protected Client client() {
        return client;
    }

    @Override
    protected Entity<?> createMultipartEntity(BreezeHttpRequest request, BreezeHttpMultipart multipart) {
        MediaType mediaType = MediaType.valueOf(request.contentType());
        MultiPart jerseyMultiPart = new MultiPart(mediaType);

        for (BreezeHttpMultipartPart part : multipart.parts()) {
            MediaType partMediaType = MediaType.valueOf(part.contentType());

            if (part.body() instanceof File) {
                jerseyMultiPart.bodyPart(new FileDataBodyPart(
                        part.name(),
                        (File) part.body(),
                        partMediaType
                ));
            } else if (part.body() instanceof InputStream) {
                jerseyMultiPart.bodyPart(new StreamDataBodyPart(
                        part.name(),
                        (InputStream) part.body(),
                        part.filename(),
                        partMediaType
                ));
            } else {
                jerseyMultiPart.bodyPart(new FormDataBodyPart(
                        part.name(),
                        part.body(),
                        partMediaType
                ));
            }
        }

        return Entity.entity(jerseyMultiPart, mediaType);
    }
}
