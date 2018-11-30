package org.lendingclub.http.breeze.client.okhttp3;

import okhttp3.FormBody;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okio.ByteString;
import org.lendingclub.http.breeze.client.AbstractInvokingBreezeHttpClient;
import org.lendingclub.http.breeze.client.okhttp3.request.InputStreamRequestBody;
import org.lendingclub.http.breeze.client.okhttp3.response.BreezeHttpOk3RawResponse;
import org.lendingclub.http.breeze.converter.BreezeHttpConverter;
import org.lendingclub.http.breeze.error.BreezeHttpErrorHandler;
import org.lendingclub.http.breeze.exception.BreezeHttpException;
import org.lendingclub.http.breeze.exception.BreezeHttpExecutionException;
import org.lendingclub.http.breeze.filter.BreezeHttpFilter;
import org.lendingclub.http.breeze.json.BreezeHttpJsonMapper;
import org.lendingclub.http.breeze.logging.BreezeHttpRequestLogger;
import org.lendingclub.http.breeze.request.BreezeHttpRequest;
import org.lendingclub.http.breeze.request.body.BreezeHttpForm;
import org.lendingclub.http.breeze.request.body.BreezeHttpMultipart;
import org.lendingclub.http.breeze.request.body.BreezeHttpMultipartPart;
import org.lendingclub.http.breeze.response.BreezeHttpRawResponse;
import org.lendingclub.http.breeze.util.BreezeHttpUtil;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Map;

import static org.lendingclub.http.breeze.util.BreezeHttpUtil.quote;

public class BreezeHttpOk3Client extends AbstractInvokingBreezeHttpClient {
    private final OkHttpClient okClient;
    private final BreezeHttpJsonMapper jsonMapper;

    public BreezeHttpOk3Client() {
        this(null, null, null, null, null, null);
    }

    public BreezeHttpOk3Client(
            OkHttpClient okClient,
            BreezeHttpRequestLogger requestLogger,
            Collection<BreezeHttpFilter> filters,
            Collection<BreezeHttpConverter> converters,
            BreezeHttpErrorHandler errorHandler,
            BreezeHttpJsonMapper jsonMapper
    ) {
        super(requestLogger, filters, converters, errorHandler);
        this.okClient = okClient != null ? okClient : new OkHttpClient();
        this.jsonMapper = jsonMapper != null ? jsonMapper : BreezeHttpJsonMapper.findMapper();
    }

    public OkHttpClient okClient() {
        return okClient;
    }

    public BreezeHttpJsonMapper jsonMapper() {
        return jsonMapper;
    }

    @Override
    protected BreezeHttpRawResponse invoke(BreezeHttpRequest request) {
        try {
            Response okResponse = okClient.newCall(createOkRequest(request)).execute();
            return new BreezeHttpOk3RawResponse(request, okResponse, this);
        } catch (IOException t) {
            throw new BreezeHttpExecutionException(request, null, null, t);
        }
    }

    protected Request createOkRequest(BreezeHttpRequest request) {
        // OkHttp3 magically handles gzip already; if you explicitly set the
        // header, you have to parse the byte[] yourself.
        if ("gzip".equals(request.header("Accept-Encoding")) && request.returnType() != byte[].class) {
            request.header("Accept-Encoding", null);
        }

        // Interpolate path variables
        String path = request.path();
        for (Map.Entry<String, Object> entry : request.pathVariables().entrySet()) {
            path = path.replaceAll("\\{" + entry.getKey() + "}", entry.getValue().toString());
        }

        // Create base URL
        HttpUrl httpUrl = HttpUrl.parse(request.url());
        if (httpUrl == null) {
            throw new BreezeHttpException("unable to parse url=" + quote(request.url()));
        }

        // Add unencoded path, preventing double forward slash
        if (path.startsWith("/")) {
            path = path.substring(1);
        }
        HttpUrl.Builder urlBuilder = httpUrl.newBuilder().addPathSegments(path);

        // Add query string
        request.queryParameters().forEach((key, values) ->
                values.forEach(value -> urlBuilder.addQueryParameter(key, value.toString()))
        );

        // Add headers
        Request.Builder requestBuilder = new Request.Builder().url(urlBuilder.build().toString());
        request.headers().forEach(
                (key, values) -> values.forEach(value -> requestBuilder.addHeader(key, value))
        );

        // Add method and body
        requestBuilder.method(request.method().toString(), convertRequestBody(request));

        // Done
        return requestBuilder.build();
    }

    protected RequestBody convertRequestBody(BreezeHttpRequest request) {
        if (request.body() == null || request.body() instanceof Void) {
            return null;
        }

        Object body = request.body();

        if (body instanceof BreezeHttpForm) {
            BreezeHttpForm form = (BreezeHttpForm) body;
            FormBody.Builder formBuilder = new FormBody.Builder();
            form.params().forEach(formBuilder::add);
            return formBuilder.build();
        }

        if (body instanceof BreezeHttpMultipart) {
            BreezeHttpMultipart multipart = (BreezeHttpMultipart) body;

            MediaType mediaType = MediaType.parse(request.contentType());
            MultipartBody.Builder builder = new MultipartBody.Builder();
            if (mediaType != null) {
                builder.setType(mediaType);
            }

            for (BreezeHttpMultipartPart part : multipart.parts()) {
                final RequestBody requestBody;
                if (part.body() instanceof InputStream) {
                    requestBody = RequestBody.create(
                            MediaType.parse(part.contentType()),
                            BreezeHttpUtil.readBytes((InputStream) part.body())
                    );
                } else {
                    requestBody = createOkRequestBody(part.contentType(), part.body());
                }
                builder.addFormDataPart(part.name(), part.filename(), requestBody);
            }
            return builder.build();
        }

        return createOkRequestBody(request.contentType(), body);
    }

    protected RequestBody createOkRequestBody(String contentType, Object body) {
        MediaType mediaType = MediaType.parse(contentType);

        if (body instanceof RequestBody) {
            return (RequestBody) body;
        }

        if (body instanceof File) {
            return RequestBody.create(mediaType, (File) body);
        }

        if (body instanceof ByteString) {
            return RequestBody.create(mediaType, (ByteString) body);
        }

        if (body instanceof byte[]) {
            return RequestBody.create(mediaType, (byte[]) body);
        }

        if (body instanceof InputStream) {
            return new InputStreamRequestBody(mediaType, (InputStream) body);
        }

        if (mediaType == null
                || (mediaType.type().equals("application") && mediaType.subtype().equals("json"))) {
            String json = body instanceof CharSequence ? body.toString() : jsonMapper.toJson(body);
            return RequestBody.create(mediaType, json);
        }

        if (mediaType.type().equals("text") && body instanceof CharSequence) {
            return RequestBody.create(mediaType, body.toString());
        }

        throw new BreezeHttpException("unable to convert request contentType=" + contentType);
    }
}
