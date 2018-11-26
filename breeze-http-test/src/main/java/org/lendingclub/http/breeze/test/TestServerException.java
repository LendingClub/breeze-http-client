package org.lendingclub.http.breeze.test;

public class TestServerException extends RuntimeException {
    public static final long serialVersionUID = -1;

    private ErrorResponse errorResponse;

    public TestServerException(String message, int code) {
        super(message);
        errorResponse = new ErrorResponse(code, message);
    }

    public int code() {
        return errorResponse.getCode();
    }

    public ErrorResponse errorResponse() {
        return errorResponse;
    }
}
