package org.lendingclub.http.breeze.request.body;

import static org.lendingclub.http.breeze.util.BreezeHttpUtil.quote;

public class BreezeHttpMultipartPart {
    private String name;
    private String contentType;
    private String filename;
    private Object body;

    public BreezeHttpMultipartPart() {
    }

    public BreezeHttpMultipartPart(String name, String contentType, String filename, Object body) {
        this.name = name;
        this.contentType = contentType;
        this.filename = filename;
        this.body = body;
    }

    public String name() {
        return name;
    }

    public void name(String name) {
        this.name = name;
    }

    public String contentType() {
        return contentType;
    }

    public void contentType(String contentType) {
        this.contentType = contentType;
    }

    public String filename() {
        return filename;
    }

    public void filename(String filename) {
        this.filename = filename;
    }

    public Object body() {
        return body;
    }

    public void body(Object body) {
        this.body = body;
    }

    public boolean isEmpty() {
        return contentType == null
                && name == null
                && filename == null
                && body == null;
    }

    @Override
    public String toString() {
        return "BreezeHttpMultipartPart{"
                + "name=" + quote(name)
                + " contentType=" + quote(contentType)
                + " filename=" + quote(filename)
                + " body=" + (body == null ? null : quote(body.getClass().getName()))
                + "}";
    }
}
