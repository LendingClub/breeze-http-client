package org.lendingclub.http.breeze.decorator;

import org.lendingclub.http.breeze.BreezeHttp;
import org.lendingclub.http.breeze.client.AbstractDecoratedBreezeHttpClient;
import org.lendingclub.http.breeze.request.BreezeHttpRequest;

import java.util.Map;

public class BreezeHttpRequestDefaultsDecorator implements BreezeHttpDecorator {
    private final BreezeHttpRequest defaultRequest;

    public BreezeHttpRequestDefaultsDecorator(BreezeHttpRequest defaultRequest) {
        this.defaultRequest = defaultRequest;
    }

    public BreezeHttpRequestDefaultsDecorator(String url) {
        this.defaultRequest = new BreezeHttpRequest().url(url);
    }

    public BreezeHttpRequestDefaultsDecorator(String url, String service) {
        this.defaultRequest = new BreezeHttpRequest().url(url).service(service);
    }

    public BreezeHttpRequestDefaultsDecorator(String url, String service, String header, String value) {
        this.defaultRequest = new BreezeHttpRequest().url(url).service(service).header(header, value);
    }

    public BreezeHttpRequestDefaultsDecorator(String url, String service, Map<String, String> headers) {
        this.defaultRequest = new BreezeHttpRequest().url(url).service(service).headers(headers);
    }

    @Override
    public BreezeHttp decorate(BreezeHttp breeze) {
        return new RequestDefaultsDecoratedClient(breeze);
    }

    public class RequestDefaultsDecoratedClient extends AbstractDecoratedBreezeHttpClient {
        public RequestDefaultsDecoratedClient(BreezeHttp breeze) {
            super(breeze, BreezeHttpRequestDefaultsDecorator.this, request -> true);
        }

        @Override
        protected <T> T decorate(BreezeHttpRequest request, DecoratorCommand<T> command) {
            if (request.url() == null) {
                request.url(defaultRequest.url());
            }

            if (request.service() == null) {
                request.service(defaultRequest.service());
            }

            if (request.name() == null) {
                request.name(defaultRequest.name());
            }

            if (request.path() == null) {
                request.path(defaultRequest.path());
            }

            defaultRequest.pathVariables().forEach((name, value) -> {
                if (request.pathVariable(name) == null) {
                    request.pathVariable(name, value);
                }
            });

            defaultRequest.queryParameters().forEach((name, values) -> {
                if (request.queryParameter(name) == null) {
                    request.queryParameter(name, values);
                }
            });

            defaultRequest.headers().forEach((name, values) -> {
                if (request.header(name) == null) {
                    values.forEach(value -> request.header(name, value, defaultRequest.headerLoggableValue(name)));
                }
            });

            if (request.body() == null) {
                request.body(defaultRequest.body());
            }

            if (request.method() == null) {
                request.method(defaultRequest.method());
            }

            if (request.returnType() == null) {
                request.returnType(defaultRequest.returnType());
            }

            return command.execute(request);
        }
    }
}
