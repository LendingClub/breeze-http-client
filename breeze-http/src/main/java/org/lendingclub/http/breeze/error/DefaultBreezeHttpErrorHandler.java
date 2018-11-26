package org.lendingclub.http.breeze.error;

import org.lendingclub.http.breeze.converter.BreezeHttpBodyConverter;
import org.lendingclub.http.breeze.exception.BreezeHttpResponseException;
import org.lendingclub.http.breeze.request.BreezeHttpRequest;
import org.lendingclub.http.breeze.response.BreezeHttpRawResponse;
import org.lendingclub.http.breeze.response.BreezeHttpResponse;

import java.lang.reflect.Type;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import static java.util.Arrays.asList;
import static org.lendingclub.http.breeze.exception.BreezeHttpResponseException.create;

public class DefaultBreezeHttpErrorHandler implements BreezeHttpErrorHandler {
    protected final Set<Integer> errorStatuses = new HashSet<>();
    protected final Type errorResponseBodyType;

    public DefaultBreezeHttpErrorHandler() {
        this(String.class);
    }

    public DefaultBreezeHttpErrorHandler(Type errorResponseBodyType) {
        this(null, errorResponseBodyType);
    }

    public DefaultBreezeHttpErrorHandler(
            Collection<Integer> errorStatuses,
            Type errorResponseBodyType
    ) {
        if (errorStatuses == null) {
            this.errorStatuses.addAll(asList(0, 4, 5));
        } else {
            this.errorStatuses.addAll(errorStatuses);
        }

        this.errorResponseBodyType = errorResponseBodyType;
    }

    @Override
    public boolean isError(BreezeHttpRequest request, BreezeHttpRawResponse raw) {
        return errorStatuses.contains(raw.httpStatus()) || errorStatuses.contains(raw.httpStatusClass().ordinal());
    }

    @Override
    public void handleError(BreezeHttpRequest request, BreezeHttpRawResponse raw) {
        BreezeHttpResponseException exception;
        try {
            BreezeHttpResponse<?> response = BreezeHttpBodyConverter.convertResponse(
                    request,
                    raw,
                    errorResponseBodyType
            );
            exception = create(request, response);
        } catch (Throwable t) {
            throw create(request, new BreezeHttpResponse<>(null, raw), t);
        }
        throw exception;
    }
}