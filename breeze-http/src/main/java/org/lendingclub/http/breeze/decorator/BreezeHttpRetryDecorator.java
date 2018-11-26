package org.lendingclub.http.breeze.decorator;

import org.lendingclub.http.breeze.BreezeHttp;
import org.lendingclub.http.breeze.client.AbstractDecoratedBreezeHttpClient;
import org.lendingclub.http.breeze.exception.BreezeHttpException;
import org.lendingclub.http.breeze.exception.BreezeHttpServerErrorException;
import org.lendingclub.http.breeze.request.BreezeHttpRequest;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.function.Predicate;
import java.util.logging.Logger;

import static java.util.stream.Collectors.toList;

/**
 * Simple retry decorator; takes a list of millisecond delays to pause between
 * failures.
 */
public class BreezeHttpRetryDecorator implements BreezeHttpDecorator {
    private final Logger logger = Logger.getLogger(getClass().getName());

    private final Predicate<BreezeHttpRequest> predicate;
    private final List<Long> retryDelays = new ArrayList<>();
    private final Sleeper sleeper;
    private final RetryableTest retryableTest;

    public BreezeHttpRetryDecorator(long... retryDelays) {
        this(Arrays.stream(retryDelays).boxed().collect(toList()));
    }

    public BreezeHttpRetryDecorator(List<Long> retryDelays) {
        this(request -> true, retryDelays, new DefaultRetryableTest());
    }

    public BreezeHttpRetryDecorator(
            Predicate<BreezeHttpRequest> predicate,
            List<Long> retryDelays,
            RetryableTest retryableTest
    ) {
        this(predicate, retryDelays, retryableTest, new Sleeper());
    }

    public BreezeHttpRetryDecorator(
            Predicate<BreezeHttpRequest> predicate,
            List<Long> retryDelays,
            RetryableTest retryableTest,
            Sleeper sleeper
    ) {
        this.predicate = predicate;
        this.retryDelays.addAll(retryDelays);
        this.retryableTest = retryableTest;
        this.sleeper = sleeper;
    }

    @Override
    public BreezeHttp decorate(BreezeHttp client) {
        return new RetryDecoratedClient(client, predicate);
    }

    public interface RetryableTest {
        boolean shouldRetry(BreezeHttpRequest request, Throwable t);
    }

    public static class DefaultRetryableTest implements RetryableTest {
        @Override
        public boolean shouldRetry(BreezeHttpRequest request, Throwable t) {
            return !(request.body() instanceof InputStream) && t instanceof BreezeHttpServerErrorException;
        }
    }

    /** This class exists for unit tests, but maybe somebody someday will want to override it. */
    public static class Sleeper {
        public void sleep(long milliseconds) {
            try {
                Thread.sleep(milliseconds);
            } catch (InterruptedException e) {
                throw new BreezeHttpException(e);
            }
        }
    }

    public class RetryDecoratedClient extends AbstractDecoratedBreezeHttpClient {
        RetryDecoratedClient(BreezeHttp breeze, Predicate<BreezeHttpRequest> predicate) {
            super(breeze, BreezeHttpRetryDecorator.this, predicate);
        }

        @Override
        protected <T> T decorate(BreezeHttpRequest request, DecoratorCommand<T> command) {
            Iterator<Long> iter = retryDelays.iterator();

            while (true) {
                try {
                    // Make sure to pass in a copy of the request since execution
                    // may modify the request object and we don't want it altered
                    // unintentionally the same way twice.
                    return command.execute(new BreezeHttpRequest(request));
                } catch (Throwable t) {
                    String error = t.getClass().getSimpleName();
                    if (retryableTest.shouldRetry(request, t)) {
                        if (iter.hasNext()) {
                            long delay = iter.next();
                            logger.warning(() -> "pausing " + delay + " ms after " + error + " for " + request);
                            sleeper.sleep(delay);
                            continue;
                        } else {
                            logger.warning(() -> error + " out of retries for " + request);
                        }
                    }
                    throw t;
                }
            }
        }
    }
}
