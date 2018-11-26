package org.lendingclub.http.breeze.exception;

import java.io.IOException;

public class BreezeHttpIOException extends BreezeHttpException {
    public static final long serialVersionUID = -1;

    public BreezeHttpIOException(String message) {
        super(message);
    }

    public BreezeHttpIOException(String message, Throwable cause) {
        super(message, cause);
    }

    public BreezeHttpIOException(Throwable cause) {
        super(cause);
    }

    public static IOException findIOExceptionCause(Throwable t) {
        while (t != null) {
            if (t instanceof IOException) {
                return (IOException) t;
            }
            t = t.getCause();
        }
        return null;
    }
}
