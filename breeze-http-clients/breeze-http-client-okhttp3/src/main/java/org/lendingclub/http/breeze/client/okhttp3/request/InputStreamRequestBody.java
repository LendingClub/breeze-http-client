package org.lendingclub.http.breeze.client.okhttp3.request;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import okio.BufferedSink;
import okio.Okio;
import okio.Source;

import java.io.IOException;
import java.io.InputStream;

/**
 * Shamelessly stolen from https://github.com/square/okhttp/issues/3585
 */
public class InputStreamRequestBody extends RequestBody {
    private final InputStream inputStream;
    private final MediaType contentType;

    public InputStreamRequestBody(MediaType contentType, InputStream inputStream) {
        this.contentType = contentType;
        this.inputStream = inputStream;
    }

    @Override
    public MediaType contentType() {
        return contentType;
    }

    @Override
    public long contentLength() throws IOException {
        return inputStream.available() == 0 ? -1 : inputStream.available();
    }

    @Override
    public void writeTo(BufferedSink sink) throws IOException {
        try (Source source = Okio.source(inputStream)) {
            sink.writeAll(source);
        }
    }
}
