package org.lendingclub.http.breeze.client.okhttp3.response;

import okhttp3.MediaType;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okio.BufferedSource;
import org.lendingclub.http.breeze.client.okhttp3.BreezeHttpOk3Client;
import org.lendingclub.http.breeze.exception.BreezeHttpException;
import org.lendingclub.http.breeze.exception.BreezeHttpIOException;
import org.lendingclub.http.breeze.response.BreezeHttpRawResponse;
import org.lendingclub.http.breeze.response.BreezeHttpResponseInputStream;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.lang.reflect.Type;
import java.util.ArrayList;

import static org.lendingclub.http.breeze.util.BreezeHttpUtil.cast;

public class BreezeHttpOk3RawResponse extends BreezeHttpRawResponse {
    protected final Response okResponse;
    protected final ResponseBody okBody;
    protected final BreezeHttpOk3Client breeze;
    protected final byte[] bufferedBody;

    public BreezeHttpOk3RawResponse(
            Type conversionType,
            Response okResponse,
            BreezeHttpOk3Client breeze,
            boolean bufferResponse
    ) {
        super(conversionType);
        this.okResponse = okResponse;
        this.okBody = okResponse.body();
        this.breeze = breeze;
        this.httpStatus = okResponse.code();

        try {
            if (bufferResponse && okBody != null) {
                bufferedBody = okResponse.peekBody(Integer.MAX_VALUE).bytes();
            } else {
                bufferedBody = null;
            }
        } catch (IOException e) {
            throw new BreezeHttpIOException(e);
        }

        okResponse.headers().toMultimap().forEach((key, values) -> header(key, new ArrayList<>(values)));
    }

    @Override
    public InputStream body() {
        if (okBody == null) {
            return null;
        }

        if (bufferedBody == null) {
            return new BreezeHttpResponseInputStream(okResponse, okBody.byteStream());
        }

        return new BreezeHttpResponseInputStream(okResponse, new ByteArrayInputStream(bufferedBody));
    }

    @Override
    public String string() {
        try {
            if (okBody == null) {
                return null;
            }

            if (bufferedBody == null) {
                return okBody.string();
            }

            return new String(bufferedBody);
        } catch (IOException e) {
            throw new BreezeHttpIOException(e);
        }
    }

    @Override
    public byte[] bytes() {
        try {
            if (okBody == null) {
                return null;
            }

            if (bufferedBody == null) {
                return okBody.bytes();
            }

            return bufferedBody;
        } catch (IOException e) {
            throw new BreezeHttpIOException(e);
        }
    }

    @Override
    public <T> T convert(Type type) {
        if (type == null || type == Void.class || okBody == null) {
            return null;
        }

        try {
            if (type == String.class) {
                return cast(bufferedBody == null ? okBody.string() : new String(bufferedBody));
            }

            if (type == byte[].class) {
                return cast(bufferedBody == null ? okBody.bytes() : bufferedBody);
            }

            if (type == Reader.class) {
                return cast(bufferedBody == null
                        ? okBody.charStream()
                        : new StringReader(new String(bufferedBody)));
            }

            if (type == BufferedSource.class) {
                return cast(okBody.source());
            }

            if (type == Response.class) {
                return cast(okResponse);
            }

            if (type == InputStream.class) {
                return cast(body());
            }

            MediaType mediaType = okBody.contentType();
            if (mediaType != null && "application".equals(mediaType.type()) && "json".equals(mediaType.subtype())) {
                return breeze.jsonMapper().fromJson(string(), type);
            }

            throw new BreezeHttpException("unable to convert response contentType=" + mediaType + " to " + type);
        } catch (IOException e) {
            throw new BreezeHttpIOException(e);
        }
    }

    @Override
    public void close() {
        okResponse.close();
    }
}
