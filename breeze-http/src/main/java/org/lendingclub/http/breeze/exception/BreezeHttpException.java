package org.lendingclub.http.breeze.exception;

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
}
