/*
 * Copyright (C) 2018 Lending Club, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.lendingclub.http.breeze.client;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

/**
 * Translate our test server exceptions into ResponseEntity objects.
 *
 * @author Raul Acevedo
 */
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
