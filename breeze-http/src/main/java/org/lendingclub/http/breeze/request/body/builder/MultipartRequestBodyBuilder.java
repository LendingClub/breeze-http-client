package org.lendingclub.http.breeze.request.body.builder;

import org.lendingclub.http.breeze.request.BreezeHttpRequest;
import org.lendingclub.http.breeze.request.body.BreezeHttpMultipart;
import org.lendingclub.http.breeze.request.body.BreezeHttpMultipartPart;

import java.io.File;

public class MultipartRequestBodyBuilder extends AbstractRequestBodyBuilder<BreezeHttpMultipart> {
    private BreezeHttpMultipartPart currentPart = new BreezeHttpMultipartPart();

    public MultipartRequestBodyBuilder(BreezeHttpRequest request) {
        super(request, new BreezeHttpMultipart());
    }

    public MultipartRequestBodyBuilder part() {
        saveCurrentPart();
        currentPart = new BreezeHttpMultipartPart();
        return this;
    }

    public MultipartRequestBodyBuilder part(BreezeHttpMultipartPart part) {
        saveCurrentPart();
        body.part(part);
        currentPart = new BreezeHttpMultipartPart();
        return this;
    }

    public MultipartRequestBodyBuilder part(String name, String value) {
        saveCurrentPart();
        body.part(name, "text/plain", null, value);
        currentPart = new BreezeHttpMultipartPart();
        return this;
    }

    public MultipartRequestBodyBuilder part(String name, String contentType, File file) {
        saveCurrentPart();
        body.part(name, contentType, file.getName(), file);
        currentPart = new BreezeHttpMultipartPart();
        return this;
    }

    public MultipartRequestBodyBuilder part(String name, String contentType, Object body) {
        saveCurrentPart();
        this.body.part(name, contentType, null, body);
        currentPart = new BreezeHttpMultipartPart();
        return this;
    }

    public MultipartRequestBodyBuilder contentType(String contentType) {
        currentPart.contentType(contentType);
        return this;
    }

    public MultipartRequestBodyBuilder json() {
        currentPart.contentType("application/json; charset=utf-8");
        return this;
    }

    public MultipartRequestBodyBuilder name(String name) {
        currentPart.name(name);
        return this;
    }

    public MultipartRequestBodyBuilder filename(String filename) {
        currentPart.filename(filename);
        return this;
    }

    public MultipartRequestBodyBuilder body(Object body) {
        currentPart.body(body);
        return this;
    }

    @Override
    protected void finish() {
        saveCurrentPart();
    }

    private void saveCurrentPart() {
        if (!currentPart.isEmpty()) {
            body.part(currentPart);
            currentPart = null;
        }
    }
}
