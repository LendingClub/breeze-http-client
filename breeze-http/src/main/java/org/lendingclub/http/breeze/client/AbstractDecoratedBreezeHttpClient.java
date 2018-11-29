package org.lendingclub.http.breeze.client;

import org.lendingclub.http.breeze.BreezeHttp;
import org.lendingclub.http.breeze.converter.BreezeHttpConverter;
import org.lendingclub.http.breeze.decorator.BreezeHttpDecorator;
import org.lendingclub.http.breeze.decorator.DecoratedBreezeHttp;
import org.lendingclub.http.breeze.decorator.DecoratorCommand;
import org.lendingclub.http.breeze.error.BreezeHttpErrorHandler;
import org.lendingclub.http.breeze.exception.BreezeHttpException;
import org.lendingclub.http.breeze.filter.BreezeHttpFilter;
import org.lendingclub.http.breeze.logging.BreezeHttpRequestLogger;
import org.lendingclub.http.breeze.request.BreezeHttpRequest;

import java.util.List;
import java.util.function.Predicate;

import static org.lendingclub.http.breeze.util.BreezeHttpUtil.quote;
import static org.lendingclub.http.breeze.util.BreezeHttpUtil.simpleName;

public abstract class AbstractDecoratedBreezeHttpClient extends AbstractBreezeHttpClient implements DecoratedBreezeHttp {
    protected final BreezeHttp breeze;
    protected final BreezeHttpDecorator decorator;
    protected final Predicate<BreezeHttpRequest> predicate;
    protected final String decorators;
    protected final BreezeHttp client;

    public AbstractDecoratedBreezeHttpClient(BreezeHttpDecorator decorator, BreezeHttp breeze) {
        this(breeze, decorator, request -> true);
    }

    public AbstractDecoratedBreezeHttpClient(
            BreezeHttp breeze,
            BreezeHttpDecorator decorator,
            Predicate<BreezeHttpRequest> predicate
    ) {
        this.breeze = breeze;
        this.decorator = decorator;
        this.predicate = predicate;

        // Find the original BreezeHttp that was decorated, possibly multiple times
        StringBuilder b = new StringBuilder("[").append(decorator.name());
        while (breeze instanceof DecoratedBreezeHttp) {
            DecoratedBreezeHttp commands = (DecoratedBreezeHttp) breeze;
            b.append(", ").append(commands.decorator().name());
            breeze = commands.breeze();
        }
        this.decorators = b.append("]").toString();
        this.client = breeze;
    }

    @Override
    public BreezeHttpDecorator decorator() {
        return decorator;
    }

    @Override
    public BreezeHttpRequest request() {
        return new BreezeHttpRequest("", null, this, null);
    }

    @Override
    public BreezeHttpRequest request(String url) {
        return new BreezeHttpRequest(url, null, this, null);
    }

    @Override
    public List<BreezeHttpConverter> converters() {
        return breeze.converters();
    }

    @Override
    public List<BreezeHttpFilter> filters() {
        return breeze.filters();
    }

    @Override
    public BreezeHttpRequestLogger requestLogger() {
        return breeze.requestLogger();
    }

    @Override
    public BreezeHttpErrorHandler errorHandler() {
        return breeze.errorHandler();
    }

    @Override
    public <T> T execute(BreezeHttpRequest request) throws BreezeHttpException {
        return execute(request, command -> breeze.execute(request));
    }

    /** Decorate the command if the request matches, otherwise execute it normally. */
    protected <T> T execute(BreezeHttpRequest request, DecoratorCommand<T> command) {
        if (predicate.test(request)) {
            return decorate(request, command);
        } else {
            return command.execute(request);
        }
    }

    /**
     * Execute a decorated BreezeHttp command.
     *
     * @param request original request object
     * @param command command to execute
     * @param <T> return type
     * @return object of type T
     */
    protected abstract <T> T decorate(BreezeHttpRequest request, DecoratorCommand<T> command);

    @Override
    public BreezeHttp breeze() {
        return breeze;
    }

    @Override
    public BreezeHttp client() {
        return client;
    }

    /** Splunk-friendly toString: more intuitive to search for BreezeHttp than implementation names. */
    @Override
    public String toString() {
        return "BreezeHttp{client=" + simpleName(client) + ", decorators=" + quote(decorators) + "}";
    }
}
