package org.lendingclub.http.breeze.filter;

import org.lendingclub.http.breeze.client.json.BreezeHttpJsonMapper;
import org.lendingclub.http.breeze.exception.BreezeHttpException;
import org.lendingclub.http.breeze.exception.BreezeHttpExecutionException;
import org.lendingclub.http.breeze.exception.BreezeHttpResponseException;
import org.lendingclub.http.breeze.request.BreezeHttpRequest;
import org.lendingclub.http.breeze.request.body.BreezeHttpForm;
import org.lendingclub.http.breeze.request.body.BreezeHttpMultipart;
import org.lendingclub.http.breeze.response.BreezeHttpRawResponse;

import java.io.IOException;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import static org.lendingclub.http.breeze.util.BreezeHttpUtil.quote;

public class BreezeHttpDetailLoggingFilter implements BreezeHttpFilter {
    private static final Pattern EXCLUDE_REQUEST_BODY_JSON = Pattern.compile(
            "\\A(java\\.n?io\\.|okio\\.|com\\.squareup\\.|javax\\.ws\\.rs\\.|org\\.springframework\\.).+\\z"
    );

    private final Logger logger = Logger.getLogger(getClass().getSimpleName());
    private final String nl = System.lineSeparator();
    private final Level level;
    private final BreezeHttpJsonMapper jsonMapper;
    private final Predicate<BreezeHttpRequest> predicate;

    public BreezeHttpDetailLoggingFilter() {
        this(Level.INFO, BreezeHttpJsonMapper.findMapper(true), request -> true);
    }

    public BreezeHttpDetailLoggingFilter(
            Level level,
            BreezeHttpJsonMapper jsonMapper,
            Predicate<BreezeHttpRequest> predicate
    ) {
        this.level = level;
        this.jsonMapper = jsonMapper;
        this.predicate = predicate;
    }

    @Override
    public boolean shouldFilter(BreezeHttpRequest request) {
        return predicate.test(request);
    }

    @Override
    public boolean created(BreezeHttpRequest request) {
        request.bufferResponse(true);
        return true;
    }

    @Override
    public boolean setup(BreezeHttpRequest request) {
        try {
            if (!logger.isLoggable(level)) {
                return true;
            }

            request.bufferResponse(true);
            StringBuilder b = new StringBuilder()
                    .append(nl).append(nl).append("==================== Setup ")
                    .append(request.toStringShort())
                    .append(" ====================")
                    .append(nl).append("url           : ").append(request.url())
                    .append(nl).append("service       : ").append(request.service())
                    .append(nl).append("name          : ").append(request.name())
                    .append(nl).append("method        : ").append(request.method())
                    .append(nl).append("path          : ").append(request.path());

            request.pathVariables().forEach((key, value) ->
                    b.append(nl).append("pathVariable  : ").append(key).append("=").append(quote(value))
            );

            request.queryParameters().forEach((key, values) -> values.forEach(value ->
                    b.append(nl).append("queryParameter: ").append(key).append("=").append(quote(value))
            ));

            request.headers().forEach((key, values) -> values.forEach(value ->
                    b.append(nl).append("header        : ").append(key).append(": ").append(value)
            ));

            logRequestBody(request, b);
            b.append(nl);

            logger.log(level, b.toString());
        } catch (Throwable t) {
            logger.log(level, t + " logging request", t);
        }

        return true;
    }

    protected void logRequestBody(BreezeHttpRequest request, StringBuilder b) {
        if (request.body() == null) {
            b.append(nl).append("-------------------- request body: null --------------------");
        } else if (EXCLUDE_REQUEST_BODY_JSON.matcher(request.body().getClass().getName()).matches()) {
            b.append(nl).append("-------------------- request body: ");
            b.append(request.body().getClass());
            b.append(" --------------------");
        } else {
            if (request.body() instanceof BreezeHttpForm) {
                b.append(nl).append("-------------------- request body: form --------------------");
                ((BreezeHttpForm) request.body()).params().forEach((key, value) ->
                        b.append(nl).append(key).append("=").append(quote(value))
                );
            } else if (request.body() instanceof BreezeHttpMultipart) {
                b.append(nl).append("-------------------- request body: multipart --------------------");
                ((BreezeHttpMultipart) request.body()).parts().forEach(part -> {
                    b.append(nl).append("part name=").append(quote(part.name()));
                    b.append(" contentType=").append(quote(part.contentType()));

                    if (part.body() instanceof String) {
                        b.append(" body=").append(quote(part.body()));
                    } else if (part.body() == null) {
                        b.append(" body=null");
                    } else {
                        b.append(" bodyClass=").append(quote(part.body().getClass().getName()));
                    }

                    if (part.filename() != null) {
                        b.append(" filename=").append(quote(part.filename()));
                    }
                });
            } else if (isJson(request.contentType())) {
                b.append(nl).append("-------------------- request body: json --------------------");
                try {
                    if (request.body() instanceof CharSequence) {
                        b.append(nl).append(jsonMapper.toJson(jsonMapper.parse(request.body().toString())));
                    } else {
                        b.append(nl).append(jsonMapper.toJson(request.body()));
                    }
                } catch (Throwable t) {
                    b.append(nl).append(t);
                }
            } else {
                b.append(nl).append("-------------------- request body --------------------");
                b.append(nl).append(request.body());
            }
            b.append(nl).append("-------------------- request body --------------------");
        }
    }

    @Override
    public boolean executed(BreezeHttpRequest request, BreezeHttpRawResponse raw) {
        try {
            if (!logger.isLoggable(level)) {
                return true;
            }

            StringBuilder b = new StringBuilder()
                    .append(nl).append(nl).append("==================== Executed ")
                    .append(request.toStringShort())
                    .append(" ====================")
                    .append(nl).append("httpStatus   : ").append(raw.httpStatus());

            raw.headers().forEach((key, values) -> values.forEach(value ->
                    b.append(nl).append("header       : ").append(key).append(": ").append(value)
            ));

            b.append(nl).append("response type: ").append(request.returnType().getTypeName());
            logResponseBody(request, raw, b);
            b.append(nl);

            logger.log(level, b.toString());
        } catch (Throwable t) {
            logger.log(level, t + " logging response", t);
        }

        return true;
    }

    protected void logResponseBody(BreezeHttpRequest request, BreezeHttpRawResponse raw, StringBuilder b) {
        if (raw.body() == null) {
            b.append(nl).append("-------------------- response body: null --------------------");
        } else if (isJson(raw.contentType())) {
            b.append(nl).append("-------------------- response body: json --------------------").append(nl);
            try {
                b.append(jsonMapper.toJson(jsonMapper.parse(raw.string())));
            } catch (Throwable t) {
                b.append(t);
            }
            b.append(nl).append("-------------------- response body --------------------");
        } else if (isText(raw.contentType())) {
            b.append(nl).append("-------------------- response body --------------------").append(nl);
            b.append(raw.string());
            b.append(nl).append("-------------------- response body --------------------");
        } else {
            b.append(nl).append("-------------------- response body: bytes[");
            b.append(raw.bytes().length);
            b.append("] --------------------");
        }
    }

    @Override
    public boolean exception(BreezeHttpExecutionException e) {
        Throwable cause = e.getCause();
        BreezeHttpRequest request = e.request();

        try {
            if (!logger.isLoggable(level)) {
                return true;
            }

            StringBuilder b = new StringBuilder()
                    .append(nl).append(nl).append("==================== Exception processing ")
                    .append(request.toStringShort())
                    .append(" ====================")
                    .append(nl).append("error    : ").append(e)
                    .append(nl).append("cause    : ").append(cause);
            IOException io = BreezeHttpException.findIOException(cause);
            if (io != null) {
                b.append(nl).append("i/o cause: ").append(io);
            }

            if (e instanceof BreezeHttpResponseException) {
                Object body = ((BreezeHttpResponseException) e).body();
                b.append(nl).append("response : ").append(body == null ? "null" : body.getClass().getName());
                if (body != null) {
                    b.append(nl).append("-------------------- response class --------------------").append(nl);
                    b.append(body.toString());
                    b.append(nl).append("-------------------- response class --------------------");
                }
            }
            b.append(nl);

            logger.log(level, b.toString());
        } catch (Throwable oops) {
            logger.log(level, oops + " logging exception", oops);
        }

        return true;
    }

    protected boolean isJson(String contentType) {
        return contentType != null && contentType.contains("application/json");
    }

    protected boolean isText(String contentType) {
        if (contentType == null) {
            return false;
        }

        return contentType.startsWith("text/")
                || contentType.startsWith("application/x-www-form-urlencoded")
                || contentType.startsWith("application/javascript")
                || contentType.startsWith("application/xml")
                || contentType.startsWith("application/xhtml+xml");
    }
}
