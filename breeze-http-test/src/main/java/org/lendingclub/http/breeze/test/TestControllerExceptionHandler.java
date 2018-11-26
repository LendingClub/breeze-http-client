package org.lendingclub.http.breeze.test;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class TestControllerExceptionHandler {
    /**
     * Append extra field to ErrorResponse to make sure clients ignore new JSON fields,
     * rather than throw an exception during parsing.
     */
    private static class ServerErrorResponse extends ErrorResponse {
        private String extraField = "extra error field";

        ServerErrorResponse(ErrorResponse errorResponse) {
            super(errorResponse.getCode(), errorResponse.getMessage());
        }

        String getExtraField() {
            return extraField;
        }

        void setExtraField(String extraField) {
            this.extraField = extraField;
        }
    }

    @ExceptionHandler({TestServerException.class})
    public ResponseEntity<ErrorResponse> handle(TestServerException e) {
        ErrorResponse errorResponse = e.errorResponse();
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_TYPE, "application/json;charset=UTF-8");
        return new ResponseEntity<>(
                new ServerErrorResponse(errorResponse),
                headers,
                HttpStatus.valueOf(e.code())
        );
    }
}
