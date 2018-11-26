package org.lendingclub.http.breeze.response;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;

public class BreezeHttpResponseInputStream extends InputStream {
    protected final Closeable source;
    protected final InputStream stream;

    public BreezeHttpResponseInputStream(Closeable source, InputStream stream) {
        this.source = source;
        this.stream = stream;
    }

    public Closeable source() {
        return source;
    }

    public InputStream delegate() {
        return stream;
    }

    @Override
    public void close() {
        silentClose(stream);
        silentClose(source);
    }

    private void silentClose(Closeable closeable) {
        try {
            closeable.close();
        } catch (Throwable t) {
            // ignore it
        }
    }

    @Override
    public int read(byte b[]) throws IOException {
        return stream.read(b);
    }

    @Override
    public int read() throws IOException {
        return stream.read();
    }

    @Override
    public int read(byte b[], int off, int len) throws IOException {
        return stream.read(b, off, len);
    }

    @Override
    public long skip(long n) throws IOException {
        return stream.skip(n);
    }

    @Override
    public int available() throws IOException {
        return stream.available();
    }

    @Override
    public synchronized void mark(int readlimit) {
        stream.mark(readlimit);
    }

    @Override
    public synchronized void reset() throws IOException {
        stream.reset();
    }

    @Override
    public boolean markSupported() {
        return stream.markSupported();
    }
}

