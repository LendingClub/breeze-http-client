package org.lendingclub.http.breeze.decorator;

import org.lendingclub.http.breeze.exception.BreezeHttpException;
import org.lendingclub.http.breeze.request.BreezeHttpRequest;

/**
 * Simple command interface for decorators to invoke the underlying client.
 * Decorators may pass an altered version, or a copy, of the original request
 * object as necessary.
 */
public interface DecoratorCommand<T> {
    T execute(BreezeHttpRequest request) throws BreezeHttpException;
}
