package org.lendingclub.http.breeze.request.body;

import java.util.ArrayList;
import java.util.List;

public class BreezeHttpMultipart {
    private final List<BreezeHttpMultipartPart> parts = new ArrayList<>();

    public BreezeHttpMultipart() {
    }

    public BreezeHttpMultipart(List<BreezeHttpMultipartPart> parts) {
        this.parts.addAll(parts);
    }

    public BreezeHttpMultipart parts(List<BreezeHttpMultipartPart> parts) {
        this.parts.addAll(parts);
        return this;
    }

    public BreezeHttpMultipart part(BreezeHttpMultipartPart part) {
        parts.add(part);
        return this;
    }

    public BreezeHttpMultipart part(String name, String contentType, String filename, Object body) {
        parts.add(new BreezeHttpMultipartPart(name, contentType, filename, body));
        return this;
    }

    public List<BreezeHttpMultipartPart> parts() {
        return parts;
    }

    @Override
    public String toString() {
        return "BreezeHttpMultipart{parts=" + parts + "}";
    }
}
