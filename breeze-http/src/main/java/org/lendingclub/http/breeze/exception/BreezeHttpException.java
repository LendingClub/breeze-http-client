package org.lendingclub.http.breeze.exception;

import java.io.IOException;

public class BreezeHttpException extends RuntimeException {
    public static final long serialVersionUID = -1;

    public BreezeHttpException(String message) {
        super(message);
    }

    public BreezeHttpException(String message, Throwable cause) {
        super(message, cause);
    }

    public BreezeHttpException(Throwable cause) {
        super(cause);
    }

    public IOException findIOException() {
        return findIOException(this);
    }

    public static IOException findIOException(Throwable t) {
        while (t != null) {
            if (t instanceof IOException) {
                return (IOException) t;
            }
            t = t.getCause();
        }
        return null;
    }
}
