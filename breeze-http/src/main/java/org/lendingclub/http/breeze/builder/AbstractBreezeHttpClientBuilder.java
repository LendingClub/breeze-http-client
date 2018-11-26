package org.lendingclub.http.breeze.builder;

import org.lendingclub.http.breeze.BreezeHttp;
import org.lendingclub.http.breeze.converter.BreezeHttpBodyConverter;
import org.lendingclub.http.breeze.decorator.BreezeHttpDecorator;
import org.lendingclub.http.breeze.error.BreezeHttpErrorHandler;
import org.lendingclub.http.breeze.error.DefaultBreezeHttpErrorHandler;
import org.lendingclub.http.breeze.filter.BreezeHttpFilter;
import org.lendingclub.http.breeze.logging.BreezeHttpRequestLogger;
import org.lendingclub.http.breeze.logging.DefaultBreezeHttpRequestLogger;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Logger;

public abstract class AbstractBreezeHttpClientBuilder<T> {
    protected T me;
    protected BreezeHttpRequestLogger requestLogger;
    protected List<BreezeHttpBodyConverter> converters = new ArrayList<>();
    protected List<BreezeHttpFilter> filters = new ArrayList<>();
    protected List<BreezeHttpDecorator> decorators = new ArrayList<>();
    protected BreezeHttpErrorHandler errorHandler;

    public T logger(Logger logger) {
        this.requestLogger = new DefaultBreezeHttpRequestLogger(logger);
        return me;
    }

    public T requestLogger(BreezeHttpRequestLogger requestLogger) {
        this.requestLogger = requestLogger;
        return me;
    }

    public T converter(BreezeHttpBodyConverter converter) {
        this.converters.add(converter);
        return me;
    }

    public T converters(Collection<BreezeHttpBodyConverter> converters) {
        this.converters.addAll(converters);
        return me;
    }

    public T filter(BreezeHttpFilter filter) {
        this.filters.add(filter);
        return me;
    }

    public T filters(Collection<BreezeHttpFilter> filters) {
        this.filters.addAll(filters);
        return me;
    }

    public T decorator(BreezeHttpDecorator decorator) {
        this.decorators.add(decorator);
        return me;
    }

    public T decorators(Collection<BreezeHttpDecorator> decorators) {
        this.decorators.addAll(decorators);
        return me;
    }

    public T errorHandler(BreezeHttpErrorHandler errorHandler) {
        this.errorHandler = errorHandler;
        return me;
    }

    public T errorResponseBodyType(Type type) {
        this.errorHandler = new DefaultBreezeHttpErrorHandler(type);
        return me;
    }

    public T errorResponse(Collection<Integer> errorResponseStatuses, Type type) {
        this.errorHandler = new DefaultBreezeHttpErrorHandler(errorResponseStatuses, type);
        return me;
    }

    protected BreezeHttp decorate(BreezeHttp breeze) {
        for (BreezeHttpDecorator decorator : decorators) {
            breeze = breeze.decorate(decorator);
        }
        return breeze;
    }

    public abstract BreezeHttp build();

    /** Shamelesesly stolen from Apache HttpClient. */
    public static class NoopHostnameVerifier implements HostnameVerifier {
        @Override
        public boolean verify(String s, SSLSession sslSession) {
            return true;
        }

        @Override
        public final String toString() {
            return "NO_OP";
        }
    }
}
