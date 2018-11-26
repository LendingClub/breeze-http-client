package org.lendingclub.http.breeze.test;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.lendingclub.http.breeze.util.BreezeHttpUtil.quote;

public class TestMultipartResult {
    public String name;
    public String filename;
    public String contentType;
    public long size;
    public Map<String, String> headers = new LinkedHashMap<>();

    public TestMultipartResult() {
    }

    public TestMultipartResult(String name, String filename, String contentType, long size) {
        this.name = name;
        this.filename = filename;
        this.contentType = contentType;
        this.size = size;
    }

    public void header(String header, String value) {
        headers.put(header, value);
    }

    @Override
    public String toString() {
        return "TestMultipartResult{"
                + "name=" + quote(name)
                + ", contentType=" + quote(contentType)
                + ", size=" + size
                + ", headers=" + headers
                + "}";
    }
}
