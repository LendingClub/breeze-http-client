package org.lendingclub.http.breeze.decorator;

import org.lendingclub.http.breeze.BreezeHttp;

public interface BreezeHttpDecorator {
    BreezeHttp decorate(BreezeHttp breeze);

    default String name() {
        return getClass().getSimpleName().replaceAll("\\A(BreezeHttp)?(.+?)(Decorator)?\\z", "$2");
    }
}
