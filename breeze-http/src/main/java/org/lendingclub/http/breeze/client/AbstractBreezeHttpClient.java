package org.lendingclub.http.breeze.client;

import org.lendingclub.http.breeze.BreezeHttp;
import org.lendingclub.http.breeze.decorator.BreezeHttpDecorator;
import org.lendingclub.http.breeze.request.BreezeHttpRequest;
import org.lendingclub.http.breeze.request.BreezeHttpRequest.Method;
import org.lendingclub.http.breeze.response.BreezeHttpResponse;
import org.lendingclub.http.breeze.type.BreezeHttpType;

import java.net.URL;
import java.util.List;
import java.util.Map;

import static org.lendingclub.http.breeze.request.BreezeHttpRequest.Method.DELETE;
import static org.lendingclub.http.breeze.request.BreezeHttpRequest.Method.GET;
import static org.lendingclub.http.breeze.request.BreezeHttpRequest.Method.HEAD;
import static org.lendingclub.http.breeze.request.BreezeHttpRequest.Method.PATCH;
import static org.lendingclub.http.breeze.request.BreezeHttpRequest.Method.POST;
import static org.lendingclub.http.breeze.request.BreezeHttpRequest.Method.PUT;

/**
 * Base BreezeHttp class that takes care of delegating all methods to an
 * implementation-dependent execute() method.
 *
 * Implementations will usually subclass AbstractInvokingBreezeHttp, since
 * it handles logging and request filters; decorators will not want to
 * repeatedly call loggers/filters so they should subclass this class.
 */
public abstract class AbstractBreezeHttpClient implements BreezeHttp {
    @Override
    public BreezeHttpRequest request() {
        return new BreezeHttpRequest("", null, this, filters());
    }

    @Override
    public BreezeHttpRequest request(URL url) {
        return new BreezeHttpRequest(url, this);
    }

    @Override
    public BreezeHttpRequest request(String url) {
        return new BreezeHttpRequest(url, this);
    }

    @Override
    public BreezeHttpRequest request(BreezeHttpRequest request) {
        return new BreezeHttpRequest(request, this);
    }

    @Override
    public String get(URL url) {
        return get(new BreezeHttpRequest(url, this), String.class);
    }

    @Override
    public String get(String url) {
        return get(new BreezeHttpRequest(url, this), String.class);
    }

    @Override
    public String get(BreezeHttpRequest request) {
        return get(request, String.class);
    }

    @Override
    public <T> T get(URL url, Class<T> returnClass) {
        return execute(new BreezeHttpRequest(url, this).method(GET), null, returnClass);
    }

    @Override
    public <T> T get(String url, Class<T> returnClass) {
        return execute(new BreezeHttpRequest(url, this).method(GET), null, returnClass);
    }

    @Override
    public <T> T get(BreezeHttpRequest request, Class<T> returnClass) {
        return execute(request.method(GET), null, returnClass);
    }

    @Override
    public <T> T get(URL url, BreezeHttpType<T> returnType) {
        return execute(new BreezeHttpRequest(url, this).method(GET), null, returnType);
    }

    @Override
    public <T> T get(String url, BreezeHttpType<T> returnType) {
        return execute(new BreezeHttpRequest(url, this).method(GET), null, returnType);
    }

    @Override
    public <T> T get(BreezeHttpRequest request, BreezeHttpType<T> returnType) {
        return execute(request.method(GET), null, returnType);
    }

    @Override
    public String post(URL url, Object body) {
        return execute(new BreezeHttpRequest(url, this).method(POST), body, String.class);
    }

    @Override
    public String post(String url, Object body) {
        return execute(new BreezeHttpRequest(url, this).method(POST), body, String.class);
    }

    @Override
    public String post(BreezeHttpRequest request, Object body) {
        return execute(request.method(POST), body, String.class);
    }

    @Override
    public <T> T post(URL url, Object body, Class<T> returnClass) {
        return execute(new BreezeHttpRequest(url, this).method(POST), body, returnClass);
    }

    @Override
    public <T> T post(String url, Object body, Class<T> returnClass) {
        return execute(new BreezeHttpRequest(url, this).method(POST), body, returnClass);
    }

    @Override
    public <T> T post(BreezeHttpRequest request, Object body, Class<T> returnClass) {
        return execute(request.method(POST), body, returnClass);
    }

    @Override
    public <T> T post(URL url, Object body, BreezeHttpType<T> returnType) {
        return execute(new BreezeHttpRequest(url, this).method(POST), body, returnType);
    }

    @Override
    public <T> T post(String url, Object body, BreezeHttpType<T> returnType) {
        return execute(new BreezeHttpRequest(url, this).method(POST), body, returnType);
    }

    @Override
    public <T> T post(BreezeHttpRequest request, Object body, BreezeHttpType<T> returnType) {
        return execute(request.method(POST), body, returnType);
    }

    @Override
    public String put(URL url, Object body) {
        return execute(new BreezeHttpRequest(url, this).method(PUT), body, String.class);
    }

    @Override
    public String put(String url, Object body) {
        return execute(new BreezeHttpRequest(url, this).method(PUT), body, String.class);
    }

    @Override
    public String put(BreezeHttpRequest request, Object body) {
        return execute(request.method(PUT), body, String.class);
    }

    @Override
    public <T> T put(URL url, Object body, Class<T> returnClass) {
        return execute(new BreezeHttpRequest(url, this).method(PUT), body, returnClass);
    }

    @Override
    public <T> T put(String url, Object body, Class<T> returnClass) {
        return execute(new BreezeHttpRequest(url, this).method(PUT), body, returnClass);
    }

    @Override
    public <T> T put(BreezeHttpRequest request, Object body, Class<T> returnClass) {
        return execute(request.method(PUT), body, returnClass);
    }

    @Override
    public <T> T put(URL url, Object body, BreezeHttpType<T> returnType) {
        return execute(new BreezeHttpRequest(url, this).method(PUT), body, returnType);
    }

    @Override
    public <T> T put(String url, Object body, BreezeHttpType<T> returnType) {
        return execute(new BreezeHttpRequest(url, this).method(PUT), body, returnType);
    }

    @Override
    public <T> T put(BreezeHttpRequest request, Object body, BreezeHttpType<T> returnType) {
        return execute(request.method(PUT), body, returnType);
    }

    @Override
    public String patch(URL url, Object body) {
        return execute(new BreezeHttpRequest(url, this).method(PATCH), body, String.class);
    }

    @Override
    public String patch(String url, Object body) {
        return execute(new BreezeHttpRequest(url, this).method(PATCH), body, String.class);
    }

    @Override
    public String patch(BreezeHttpRequest request, Object body) {
        return execute(request.method(PATCH), body, String.class);
    }

    @Override
    public <T> T patch(URL url, Object body, Class<T> returnClass) {
        return execute(new BreezeHttpRequest(url, this).method(PATCH), body, returnClass);
    }

    @Override
    public <T> T patch(String url, Object body, Class<T> returnClass) {
        return execute(new BreezeHttpRequest(url, this).method(PATCH), body, returnClass);
    }

    @Override
    public <T> T patch(BreezeHttpRequest request, Object body, Class<T> returnClass) {
        return execute(request.method(PATCH), body, returnClass);
    }

    @Override
    public <T> T patch(URL url, Object body,  BreezeHttpType<T> returnType) {
        return execute(new BreezeHttpRequest(url, this).method(PATCH), body, returnType);
    }

    @Override
    public <T> T patch(String url, Object body,  BreezeHttpType<T> returnType) {
        return execute(new BreezeHttpRequest(url, this).method(PATCH), body, returnType);
    }

    @Override
    public <T> T patch(BreezeHttpRequest request, Object body,  BreezeHttpType<T> returnType) {
        return execute(request.method(PATCH), body, returnType);
    }

    @Override
    public String delete(URL url) {
        return execute(new BreezeHttpRequest(url, this).method(DELETE), null, String.class);
    }

    @Override
    public String delete(String url) {
        return execute(new BreezeHttpRequest(url, this).method(DELETE), null, String.class);
    }

    @Override
    public String delete(BreezeHttpRequest request) {
        return execute(request.method(DELETE), null, String.class);
    }

    @Override
    public String delete(URL url, Object body) {
        return execute(new BreezeHttpRequest(url, this).method(DELETE), body, String.class);
    }

    @Override
    public String delete(String url, Object body) {
        return execute(new BreezeHttpRequest(url, this).method(DELETE), body, String.class);
    }

    @Override
    public String delete(BreezeHttpRequest request, Object body) {
        return execute(request.method(DELETE), body, String.class);
    }

    @Override
    public <T> T delete(URL url, Object body, Class<T> returnClass) {
        return execute(new BreezeHttpRequest(url, this).method(DELETE), body, returnClass);
    }

    @Override
    public <T> T delete(String url, Object body, Class<T> returnClass) {
        return execute(new BreezeHttpRequest(url, this).method(DELETE), body, returnClass);
    }

    @Override
    public <T> T delete(BreezeHttpRequest request, Object body, Class<T> returnClass) {
        return execute(request.method(DELETE), body, returnClass);
    }

    @Override
    public <T> T delete(URL url, Object body, BreezeHttpType<T> returnType) {
        return execute(new BreezeHttpRequest(url, this).method(DELETE), body, returnType);
    }

    @Override
    public <T> T delete(String url, Object body, BreezeHttpType<T> returnType) {
        return execute(new BreezeHttpRequest(url, this).method(DELETE), body, returnType);
    }

    @Override
    public <T> T delete(BreezeHttpRequest request, Object body, BreezeHttpType<T> returnType) {
        return execute(request.method(DELETE), body, returnType);
    }

    @Override
    public Map<String, List<String>> head(URL url) {
        return head(new BreezeHttpRequest(url, this));
    }

    @Override
    public Map<String, List<String>> head(String url) {
        return head(new BreezeHttpRequest(url, this));
    }

    @Override
    public Map<String, List<String>> head(BreezeHttpRequest request) {
        return execute(request.method(HEAD), null, new BreezeHttpType<BreezeHttpResponse<Void>>() {}).headers();
    }

    @Override
    public <T> T execute(URL url, Method method, Object body, Class<T> returnClass) {
        return execute(new BreezeHttpRequest(url, this).method(method), body, returnClass);
    }

    @Override
    public <T> T execute(String url, Method method, Object body, Class<T> returnClass) {
        return execute(new BreezeHttpRequest(url, this).method(method), body, returnClass);
    }

    @Override
    public <T> T execute(URL url, Method method, Object body, BreezeHttpType<T> returnType) {
        return execute(new BreezeHttpRequest(url, this).method(method).body(body).returnType(returnType));
    }

    @Override
    public <T> T execute(String url, Method method, Object body, BreezeHttpType<T> returnType) {
        return execute(new BreezeHttpRequest(url, this).method(method).body(body).returnType(returnType));
    }

    @Override
    public <T> T execute(BreezeHttpRequest request, Object body, Class<T> returnClass) {
        return execute(request.body(body).returnType(returnClass));
    }

    @Override
    public <T> T execute(BreezeHttpRequest request, Object body, BreezeHttpType<T> returnType) {
        return execute(request.body(body).returnType(returnType));
    }

    @Override
    public BreezeHttp decorate(BreezeHttpDecorator decorator) {
        return decorator.decorate(this);
    }
}
