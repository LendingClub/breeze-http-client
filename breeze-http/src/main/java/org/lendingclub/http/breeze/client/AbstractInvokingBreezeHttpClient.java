package org.lendingclub.http.breeze.client;

import org.lendingclub.http.breeze.converter.BreezeHttpBodyConverter;
import org.lendingclub.http.breeze.converter.BreezeHttpGsonTypesConverter;
import org.lendingclub.http.breeze.converter.BreezeHttpJacksonTypesConverter;
import org.lendingclub.http.breeze.converter.BreezeHttpJsonPathTypesConverter;
import org.lendingclub.http.breeze.error.BreezeHttpErrorHandler;
import org.lendingclub.http.breeze.error.DefaultBreezeHttpErrorHandler;
import org.lendingclub.http.breeze.exception.BreezeHttpException;
import org.lendingclub.http.breeze.exception.BreezeHttpIOException;
import org.lendingclub.http.breeze.filter.BreezeHttpFilter;
import org.lendingclub.http.breeze.logging.BreezeHttpRequestLogger;
import org.lendingclub.http.breeze.logging.DefaultBreezeHttpRequestLogger;
import org.lendingclub.http.breeze.request.BreezeHttpRequest;
import org.lendingclub.http.breeze.request.body.BreezeHttpForm;
import org.lendingclub.http.breeze.request.body.BreezeHttpMultipart;
import org.lendingclub.http.breeze.response.BreezeHttpRawResponse;
import org.lendingclub.http.breeze.response.BreezeHttpResponse;

import java.io.Closeable;
import java.io.IOException;
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
    protected final List<BreezeHttpBodyConverter> converters = new ArrayList<>();
    protected final List<BreezeHttpFilter> filters = new ArrayList<>();
    protected final BreezeHttpErrorHandler errorHandler;

    public AbstractInvokingBreezeHttpClient(
            BreezeHttpRequestLogger requestLogger,
            Collection<BreezeHttpBodyConverter> converters,
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
    public List<BreezeHttpBodyConverter> converters() {
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
        try {
            requestLogger.requestStart(request);

            prepareRequest(request);
            raw = invoke(request);
            BreezeHttpResponse<T> response = processResponse(request, raw);
            if (!(response.body() instanceof Closeable)) {
                raw.close();
            }
            BreezeHttpFilter.invoke(filters, request, filter -> filter.complete(request, response));

            request.duration(System.currentTimeMillis() - start);
            requestLogger.requestEnd(request, response);

            return isSubclass(BreezeHttpResponse.class, request.returnType()) ? cast(response) : response.body();
        } catch (Throwable t) {
            try {
                request.duration(System.currentTimeMillis() - start);
                requestLogger.requestError(request, t);
                BreezeHttpFilter.invoke(filters, request, filter -> filter.exception(request, t));

                if (t instanceof BreezeHttpException) {
                    throw (BreezeHttpException) t;
                } else {
                    // FIXME: Use BreezeHttpResponseException.create?
                    IOException io = BreezeHttpIOException.findIOExceptionCause(t);
                    throw io == null ? new BreezeHttpException(t) : new BreezeHttpIOException(t);
                }
            } finally {
                if (raw != null) {
                    raw.close();
                }
            }
        }
    }

    protected void prepareRequest(BreezeHttpRequest request) {
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

        BreezeHttpBodyConverter.convertRequest(converters, converter -> converter.convert(request));
        BreezeHttpFilter.invoke(filters, request, filter -> filter.prepare(request));
    }

    protected <T> BreezeHttpResponse<T> processResponse(BreezeHttpRequest request, BreezeHttpRawResponse raw) {
        BreezeHttpFilter.invoke(filters, request, filter -> filter.executed(request, raw));
        if (request.returnType() == BreezeHttpRawResponse.class) {
            return cast(raw);
        }

        if (errorHandler.isError(request, raw)) {
            BreezeHttpFilter.invoke(filters, request, filter -> filter.error(request, raw));
            errorHandler.handleError(request, raw);
        }

        return BreezeHttpBodyConverter.convertResponse(request, raw, request.conversionType());
    }

    /** Splunk-friendly toString: more intuitive to search for BreezeHttp than implementation names. */
    @Override
    public String toString() {
        return "BreezeHttp{client=" + getClass().getSimpleName() + "}";
    }

    protected abstract BreezeHttpRawResponse invoke(BreezeHttpRequest request);
}
