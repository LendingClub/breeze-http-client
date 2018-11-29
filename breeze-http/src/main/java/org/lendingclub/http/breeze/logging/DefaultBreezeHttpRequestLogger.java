package org.lendingclub.http.breeze.logging;

import org.lendingclub.http.breeze.exception.BreezeHttpClientErrorException;
import org.lendingclub.http.breeze.exception.BreezeHttpException;
import org.lendingclub.http.breeze.exception.BreezeHttpExecutionException;
import org.lendingclub.http.breeze.request.BreezeHttpRequest;
import org.lendingclub.http.breeze.response.BreezeHttpRawResponse;
import org.lendingclub.http.breeze.response.BreezeHttpResponse;

import java.util.logging.Level;
import java.util.logging.Logger;

import static java.util.logging.Level.INFO;
import static java.util.logging.Level.SEVERE;
import static java.util.logging.Level.WARNING;
import static org.lendingclub.http.breeze.util.BreezeHttpUtil.simpleName;

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
    public void start(BreezeHttpRequest request) {
        if (logger != null && logger.isLoggable(level)) {
            logger.log(level, "executing " + request);
        }
    }

    @Override
    public void end(BreezeHttpRequest request, BreezeHttpResponse<?> response) {
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
    public void exception(BreezeHttpExecutionException e) {
        Throwable cause = e.getCause();
        Level logLevel = cause instanceof BreezeHttpClientErrorException && warnClientErrors ? WARNING : SEVERE;
        if (logger == null || !logger.isLoggable(logLevel)) {
            return;
        }

        BreezeHttpRequest request = e.request();
        StringBuilder msg = new StringBuilder("executed " + request
                + " success=false"
                + " error=" + simpleName(e)
                + " cause=" + simpleName(cause)
                + " networkError=").append(simpleName(BreezeHttpException.findIOException(cause)));

        BreezeHttpRawResponse raw = e.raw();
        if (raw != null) {
            msg.append(" httpStatus=").append(raw.httpStatus());
            msg.append(" httpStatusClass=").append(raw.httpStatusClass());
        }

        msg.append(" duration=").append(request.duration());

        logger.log(logLevel, msg.toString());
    }
}
