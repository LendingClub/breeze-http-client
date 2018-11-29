package org.lendingclub.http.breeze.client;

import org.lendingclub.http.breeze.converter.BreezeHttpConverter;
import org.lendingclub.http.breeze.converter.BreezeHttpGsonTypesConverter;
import org.lendingclub.http.breeze.converter.BreezeHttpJacksonTypesConverter;
import org.lendingclub.http.breeze.converter.BreezeHttpJsonPathTypesConverter;
import org.lendingclub.http.breeze.error.BreezeHttpErrorHandler;
import org.lendingclub.http.breeze.error.DefaultBreezeHttpErrorHandler;
import org.lendingclub.http.breeze.exception.BreezeHttpExecutionException;
import org.lendingclub.http.breeze.filter.BreezeHttpFilter;
import org.lendingclub.http.breeze.logging.BreezeHttpRequestLogger;
import org.lendingclub.http.breeze.logging.DefaultBreezeHttpRequestLogger;
import org.lendingclub.http.breeze.request.BreezeHttpRequest;
import org.lendingclub.http.breeze.request.body.BreezeHttpForm;
import org.lendingclub.http.breeze.request.body.BreezeHttpMultipart;
import org.lendingclub.http.breeze.response.BreezeHttpRawResponse;
import org.lendingclub.http.breeze.response.BreezeHttpResponse;

import java.io.Closeable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.lendingclub.http.breeze.BreezeHttpType.isSubclass;
import static org.lendingclub.http.breeze.util.BreezeHttpUtil.IS_GSON_PRESENT;
import static org.lendingclub.http.breeze.util.BreezeHttpUtil.IS_JACKSON_PRESENT;
import static org.lendingclub.http.breeze.util.BreezeHttpUtil.IS_JSON_PATH_PRESENT;
import static org.lendingclub.http.breeze.util.BreezeHttpUtil.cast;

public abstract class AbstractInvokingBreezeHttpClient extends AbstractBreezeHttpClient {
    protected final BreezeHttpRequestLogger requestLogger;
    protected final List<BreezeHttpConverter> converters = new ArrayList<>();
    protected final List<BreezeHttpFilter> filters = new ArrayList<>();
    protected final BreezeHttpErrorHandler errorHandler;

    public AbstractInvokingBreezeHttpClient(
            BreezeHttpRequestLogger requestLogger,
            Collection<BreezeHttpConverter> converters,
            Collection<BreezeHttpFilter> filters,
            BreezeHttpErrorHandler errorHandler
    ) {
        this.requestLogger = requestLogger != null ? requestLogger : new DefaultBreezeHttpRequestLogger();

        if (converters != null) {
            this.converters.addAll(converters);
        }
        if (IS_GSON_PRESENT) {
            this.converters.add(new BreezeHttpGsonTypesConverter());
        }
        if (IS_JACKSON_PRESENT) {
            this.converters.add(new BreezeHttpJacksonTypesConverter());
        }
        if (IS_JSON_PATH_PRESENT) {
            this.converters.add(new BreezeHttpJsonPathTypesConverter());
        }

        if (filters != null) {
            this.filters.addAll(filters);
        }

        this.errorHandler = errorHandler != null ? errorHandler : new DefaultBreezeHttpErrorHandler();
    }

    @Override
    public List<BreezeHttpConverter> converters() {
        return converters;
    }

    @Override
    public List<BreezeHttpFilter> filters() {
        return filters;
    }

    @Override
    public BreezeHttpRequestLogger requestLogger() {
        return requestLogger;
    }

    @Override
    public BreezeHttpErrorHandler errorHandler() {
        return errorHandler;
    }

    @Override
    public <T> T execute(BreezeHttpRequest request) {
        long start = System.currentTimeMillis();
        BreezeHttpRawResponse raw = null;
        BreezeHttpResponse<T> response = null;

        try {
            try {
                requestLogger.start(request);
                setup(request);
                raw = invoke(request);
                response = process(request, raw);
                complete(request, raw, response);
            } finally {
                request.duration(System.currentTimeMillis() - start);
            }
            requestLogger.end(request, response);
        } catch (Throwable t) {
            BreezeHttpExecutionException e = BreezeHttpExecutionException.create(request, raw, response, t);
            requestLogger.exception(e);
            BreezeHttpFilter.filter(e.request(), filter -> filter.exception(e));
            response = errorHandler.handleException(e);
        }

        return isSubclass(BreezeHttpResponse.class, request.returnType()) ? cast(response) : response.body();
    }

    protected void setup(BreezeHttpRequest request) {
        Object body = request.body();
        if (request.contentType() == null) {
            if (body instanceof BreezeHttpForm) {
                request.form();
            } else if (body instanceof BreezeHttpMultipart) {
                request.multipart();
            } else {
                request.json();
            }
        }

        for (BreezeHttpConverter converter : converters) {
            if (!converter.convertRequestBody(request)) {
                break;
            }
        }

        BreezeHttpFilter.filter(request, filter -> filter.setup(request));
    }

    protected <T> BreezeHttpResponse<T> process(BreezeHttpRequest request, BreezeHttpRawResponse raw) {
        BreezeHttpFilter.filter(request, filter -> filter.executed(request, raw));
        if (request.returnType() == BreezeHttpRawResponse.class) {
            return cast(raw);
        }

        if (errorHandler.isError(request, raw)) {
            BreezeHttpFilter.filter(request, filter -> filter.error(request, raw));
            BreezeHttpResponse<T> response = errorHandler.handleError(request, raw);
            if (response != null) {
                return response;
            }
        }

        return raw.convertResponse(request.conversionType());
    }

    protected void complete(BreezeHttpRequest request, BreezeHttpRawResponse raw, BreezeHttpResponse<?> response) {
        if (!(response.body() instanceof Closeable)) {
            raw.close();
        }
        BreezeHttpFilter.filter(request, filter -> filter.complete(request, response));
    }

    /** Splunk-friendly toString: more intuitive to search for BreezeHttp than implementation names. */
    @Override
    public String toString() {
        return "BreezeHttp{client=" + getClass().getSimpleName() + "}";
    }

    protected abstract BreezeHttpRawResponse invoke(BreezeHttpRequest request);
}
