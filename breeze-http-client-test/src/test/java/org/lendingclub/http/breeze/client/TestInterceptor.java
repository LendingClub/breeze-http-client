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

import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Magic interceptor that logs test requests and simulates errors and delays
 * so we can test timeouts.
 *
 * @author Raul Acevedo
 */
public class TestInterceptor extends HandlerInterceptorAdapter {
    private static final Logger LOGGER = Logger.getLogger("TestInterceptor");

    /**
     * Specify forceError from within the integration test as a series to
     * consume with each invocation. This is useful for retry tests where
     * the client calls are the same, but we want the server to behave
     * differently each time.
     */
    public static Iterator<String> externalForceErrors = null;

    /**
     * Allows us to verify gzip encoding with OkHttp3, which magically handles
     * gzip but also magically removes the Content-Encoding response header.
     */
    public static String responseEncoding = null;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws InterruptedException {
        LOGGER.info("received request"
                + " url=" + request.getRequestURL()
                + " method=" + request.getMethod()
                + " forceError=" + request.getParameter("forceError")
                + " forceErrorParam=" + request.getParameter("forceErrorParam")
                + " queryString=" + request.getQueryString()
                + " headers=" + headers(request));

        final String forceError;
        String forceErrorParam = "";
        if (externalForceErrors == null || !externalForceErrors.hasNext()) {
            forceError = request.getParameter("forceError");
            forceErrorParam = request.getParameter("forceErrorParam");
            LOGGER.info("request forceError=" + forceError + " forceErrorParam=" + forceErrorParam);
        } else {
            String[] nextErrors = externalForceErrors.next().split(",");
            forceError = nextErrors[0];
            if (nextErrors.length == 2) {
                forceErrorParam = nextErrors[1];
            }
            LOGGER.info("external forceError=" + forceError + " forceErrorParam=" + forceErrorParam);
        }

        if (forceError != null) {
            switch (forceError) {
                case "clientError":
                    throw new TestServerException(forceError, 400);
                case "serverError":
                    throw new TestServerException(forceError, 500);
                case "httpError":
                    throw new TestServerException(forceError, Integer.valueOf(forceErrorParam));
                case "pause":
                    Thread.sleep(Long.valueOf(forceErrorParam));
                    break;
                default:
                    throw new IllegalStateException("unrecognized forceError=\"" + forceError + "\"");
            }
        }

        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception e) {
        responseEncoding = response.getHeader("Content-Encoding");
    }

    private Map<String, List<String>> headers(HttpServletRequest request) {
        Map<String, List<String>> headers = new HashMap<>();
        for (String header : Collections.list(request.getHeaderNames())) {
            headers.put(header, Collections.list(request.getHeaders(header)));
        }
        return headers;
    }
}
