package org.lendingclub.http.breeze.decorator;

import org.lendingclub.http.breeze.BreezeHttp;

public interface DecoratedBreezeHttp extends BreezeHttp {
    /** Get the client being decorated; may be another decorator. */
    BreezeHttp breeze();

    /** Get the client class that will execute the request at the end of the decorator chain. */
    BreezeHttp client();

    /** Get the parent decorator that created this command class. */
    BreezeHttpDecorator decorator();
}
