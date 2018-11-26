package org.lendingclub.http.breeze.logging;

import org.lendingclub.http.breeze.exception.BreezeHttpIOException;
import org.lendingclub.http.breeze.exception.BreezeHttpResponseException;
import org.lendingclub.http.breeze.request.BreezeHttpRequest;
import org.lendingclub.http.breeze.response.BreezeHttpResponse;

import java.util.logging.Level;
import java.util.logging.Logger;

import static java.util.logging.Level.INFO;
import static java.util.logging.Level.SEVERE;
import static java.util.logging.Level.WARNING;
import static org.lendingclub.http.breeze.util.BreezeHttpUtil.quote;

public class DefaultBreezeHttpRequestLogger implements BreezeHttpRequestLogger {
    private final Logger logger;
    private final Level level;
    private final boolean warnClientErrors;

    public DefaultBreezeHttpRequestLogger() {
        this(Logger.getLogger("BreezeHttp"));
    }

    public DefaultBreezeHttpRequestLogger(Logger logger) {
        this(logger, INFO, true);
    }

    public DefaultBreezeHttpRequestLogger(Logger logger, Level level, boolean warnClientErrors) {
        this.logger = logger;
        this.level = level;
        this.warnClientErrors = warnClientErrors;
    }

    @Override
    public void requestStart(BreezeHttpRequest request) {
        if (logger != null && logger.isLoggable(level)) {
            logger.log(level, "executing " + request);
        }
    }

    @Override
    public void requestEnd(BreezeHttpRequest request, BreezeHttpResponse<?> response) {
        if (logger != null && logger.isLoggable(level)) {
            logger.log(level,
                    "executed " + request
                            + " success=" + !response.isError()
                            + " httpStatus=" + response.httpStatus()
                            + " httpStatusClass=" + response.httpStatusClass()
                            + " duration=" + request.duration());
        }
    }

    @Override
    public void requestError(BreezeHttpRequest request, Throwable t) {
        boolean isClientError = (t instanceof BreezeHttpResponseException)
                && ((BreezeHttpResponseException) t).isClientError();
        if (logger == null
                || !logger.isLoggable(SEVERE)
                || (isClientError && warnClientErrors && !logger.isLoggable(WARNING))) {
            return;
        }

        StringBuilder msg = new StringBuilder("executed " + request
                + " success=false"
                + " error=" + quote(t.getClass().getSimpleName()));

        if (BreezeHttpIOException.findIOExceptionCause(t) != null) {
            msg.append(", isNetworkError=").append(BreezeHttpIOException.findIOExceptionCause(t) != null);
        }

        if (t instanceof BreezeHttpResponseException) {
            msg.append(" httpStatus=").append(((BreezeHttpResponseException) t).httpStatus());
            msg.append(" httpStatusClass=").append(((BreezeHttpResponseException) t).httpStatusClass());
        }

        msg.append(" duration=").append(request.duration());

        logger.log(isClientError && warnClientErrors ? WARNING : SEVERE, msg.toString());
    }
}
