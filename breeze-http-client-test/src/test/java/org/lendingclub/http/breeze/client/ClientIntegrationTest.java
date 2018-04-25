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

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import javax.inject.Inject;
import java.io.ByteArrayInputStream;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.lendingclub.http.breeze.client.decorator.retry.RetryDecorator;
import org.lendingclub.http.breeze.client.exception.BreezeHttpException;
import org.lendingclub.http.breeze.client.exception.BreezeHttpResponseException;
import org.lendingclub.http.breeze.client.matcher.AllRequestMatcher;

import static org.lendingclub.http.breeze.client.BreezeHttpRequest.Method.GET;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

/**
 * Integration tests that run against a real controller. For each test case, we
 * run success, error and timeout cases. Error cases test 400/500 ErrorResponses
 * and recoverability.
 *
 * Rather than have the backend TestController verify all inputs, it echoes back
 * whatever it received so the actual verification happens in this class. This
 * makes it easier to try different types of tests without having to touch both
 * the controller and this class.
 *
 * @author Raul Acevedo
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = { ClientTestApplication.class })
@WebAppConfiguration
@IntegrationTest
@TestPropertySource(properties = {
        "BREEZE_HTTP_RESTTEMPLATE_CLIENT_CONNECT_TIMEOUT=500",
        "BREEZE_HTTP_RESTTEMPLATE_CLIENT_READ_TIMEOUT=500",
        "BREEZE_HTTP_RESTTEMPLATE_CLIENT_MAX_CONNECTIONS=10",
        "BREEZE_HTTP_RESTTEMPLATE_CLIENT_MAX_CONNECTIONS_PER_ROUTE=10",
        "BREEZE_HTTP_RESTTEMPLATE_CLIENT_CONNECTION_PURGE_INTERVAL=10000",
        "BREEZE_HTTP_RESTTEMPLATE_CLIENT_IDLE_CONNECTION_TIMEOUT=2000000",
        "BREEZE_HTTP_RESTTEMPLATE_CLIENT_ERROR_RESPONSE_CLASS:org.lendingclub.http.breeze.client.ErrorResponse",

        "BREEZE_HTTP_JAXRS_JERSEY_CLIENT_CONNECT_TIMEOUT=500",
        "BREEZE_HTTP_JAXRS_JERSEY_CLIENT_READ_TIMEOUT=500",
        "BREEZE_HTTP_JAXRS_JERSEY_CLIENT_ERROR_RESPONSE_CLASS:org.lendingclub.http.breeze.client.ErrorResponse",

        "BREEZE_HTTP_JAXRS_RESTEASY_CLIENT_CONNECT_TIMEOUT=500",
        "BREEZE_HTTP_JAXRS_RESTEASY_CLIENT_SOCKET_TIMEOUT=500"
})
public class ClientIntegrationTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(ClientIntegrationTest.class);

    public static final String TEST_ROOT_URL = "http://localhost:8888/test";

    public static final String MAGIC = "[!@ +?&]";
    public static final String PATH = "path" + MAGIC;
    public static final String QUERY = "query" + MAGIC;
    public static final String QUERY_MULTIPLE_1 = "queryMultiple1" + MAGIC;
    public static final String QUERY_MULTIPLE_2 = "queryMultiple2" + MAGIC;
    public static final String HEADER = "boom";

    public static final String PATH_RESULT = "pathVariable=" + PATH;
    public static final String QUERY_RESULT = "queryVariable=" + QUERY;
    public static final String QUERY_MULTIPLE_RESULT = "queryMultiple=[" + QUERY_MULTIPLE_1 + ", " + QUERY_MULTIPLE_2 + "]";
    public static final String QUERY_ALL_RESULT = QUERY_RESULT + ", " + QUERY_MULTIPLE_RESULT;
    public static final String HEADER_RESULT = "header=" + HEADER;

    @Inject
    private BreezeHttpClient breezeHttpRestTemplateClient;

    @Inject
    private BreezeHttpClient breezeHttpJaxRsJerseyClient;

    private List<BreezeHttpClient> clients;

    @Before
    public void before() {
        clients = asList(
                breezeHttpRestTemplateClient.forService(TEST_ROOT_URL, "test"),
                breezeHttpJaxRsJerseyClient.forService(TEST_ROOT_URL, "test")
        );
    }

    private BreezeHttpRequest request(BreezeHttpClient client, String path, String forceError, String forceErrorParam) {
        return client.request()
                .path(path)
                .pathVariable("pathVariable", PATH)
                .queryVariable("queryVariable", QUERY)
                .queryVariable("queryMultiple", QUERY_MULTIPLE_1)
                .queryVariable("queryMultiple", QUERY_MULTIPLE_2)
                .header("TestHeader", HEADER)
                .queryVariable("forceError", forceError)
                .queryVariable("forceErrorParam", forceErrorParam);
    }

    @Test
    public void get() {
        runTests((client, forceError, forceErrorParam) ->
                request(client, "/get/{pathVariable}", forceError, forceErrorParam).get()
        );
    }

    @Test
    public void nullGet() {
        runTests((client, forceError, forceErrorParam) ->
                request(client, "/null", forceError, forceErrorParam).get()
        );
    }

    @Test
    public void voidGet() {
        runTests((client, forceError, forceErrorParam) ->
                request(client, "/void", forceError, forceErrorParam).get()
        );
    }

    @Test
    public void getForObject() {
        runTests((client, forceError, forceErrorParam) ->
            assertEquals(
                    PATH_RESULT + ", " + QUERY_ALL_RESULT + ", " + HEADER_RESULT,
                    request(client, "/get/{pathVariable}", forceError, forceErrorParam).get(TestModel.class).getMessage()
            )
        );
    }

    @Test
    public void getForMap() {
        runTests((client, forceError, forceErrorParam) -> {
            Map<String, List<TestModel>> expected = Collections.singletonMap(
                    PATH,
                    singletonList(new TestModel(QUERY_ALL_RESULT + ", " + HEADER_RESULT))
            );

            BreezeHttpRequest request = request(
                    client,
                    "/getMap/{pathVariable}",
                    forceError,
                    forceErrorParam
            );
            Map<String, List<TestModel>> result = request.get(new BreezeHttpType<Map<String, List<TestModel>>>() {});

            assertEquals(expected, result);
        });
    }

    @Test
    public void post() {
        runTests((client, forceError, forceErrorParam) ->
                request(client, "/post/{pathVariable}", forceError, forceErrorParam).post("this is a post")
        );
    }

    @Test
    public void postForObject() {
        runTests((client, forceError, forceErrorParam) -> {
            BreezeHttpRequest request = request(
                    client,
                    "/post/{pathVariable}",
                    forceError,
                    forceErrorParam
            );
            TestModel model = request.post(TestModel.class, "this is a post");

            assertEquals(
                    PATH_RESULT + ", " + QUERY_ALL_RESULT + ", " + HEADER_RESULT + ", body=this is a post",
                    model.getMessage()
            );
        });
    }

    @Test
    public void postForMap() {
        runTests((client, forceError, forceErrorParam) -> {
            Map<String, List<TestModel>> expected = Collections.singletonMap(
                    PATH,
                    singletonList(new TestModel(QUERY_ALL_RESULT + ", " + HEADER_RESULT + ", body=this is a post"))
            );

            BreezeHttpRequest request = request(
                    client,
                    "/postMap/{pathVariable}",
                    forceError,
                    forceErrorParam
            );
            Map<String, List<TestModel>> result = request.post(
                    new BreezeHttpType<Map<String, List<TestModel>>>() {},
                    "this is a post"
            );

            assertEquals(expected, result);
        });
    }

    @Test
    public void put() {
        runTests((client, forceError, forceErrorParam) ->
                request(client, "/put/{pathVariable}", forceError, forceErrorParam).put("this is a put")
        );
    }

    @Test
    public void putForObject() {
        runTests((client, forceError, forceErrorParam) -> {
            BreezeHttpRequest request = request(client, "/put/{pathVariable}", forceError, forceErrorParam);

            assertEquals(
                    PATH_RESULT + ", " + QUERY_ALL_RESULT + ", " + HEADER_RESULT + ", body=this is a put",
                    request.put(TestModel.class, "this is a put").getMessage()
            );
        });
    }

    @Test
    public void putForMap() {
        runTests((client, forceError, forceErrorParam) -> {
            Map<String, List<TestModel>> expected = Collections.singletonMap(
                    PATH,
                    singletonList(new TestModel(QUERY_ALL_RESULT + ", " + HEADER_RESULT + ", body=this is a put"))
            );

            BreezeHttpRequest request = request(client, "/putMap/{pathVariable}", forceError, forceErrorParam);
            Map<String, List<TestModel>> result = request.put(
                    new BreezeHttpType<Map<String, List<TestModel>>>() {},
                    "this is a put"
            );

            assertEquals(expected, result);
        });
    }

    @Test
    public void patch() throws Exception {
        runTests((client, forceError, forceErrorParam) ->
                request(client, "/patch", forceError, forceErrorParam).patch(new TestModel("message" + MAGIC))
        );
    }

    @Test
    public void patchForObject() throws Exception {
        runTests((client, forceError, forceErrorParam) -> {
            TestModel model = request(client, "/patch", forceError, forceErrorParam)
                    .patch(TestModel.class, new TestModel("message" + MAGIC));
            assertEquals("message" + MAGIC, model.getMessage());
        });
    }

    @Test
    public void patchForMap() {
        runTests((client, forceError, forceErrorParam) -> {
            Map<String, List<TestModel>> expected = Collections.singletonMap(
                    PATH,
                    singletonList(new TestModel(QUERY_ALL_RESULT + ", " + HEADER_RESULT + ", body=this is a patch"))
            );

            BreezeHttpRequest request = request(client, "/patchMap/{pathVariable}", forceError, forceErrorParam);
            Map<String, List<TestModel>> result = request.patch(
                    new BreezeHttpType<Map<String, List<TestModel>>>() {},
                    "this is a patch"
            );

            assertEquals(expected, result);
        });
    }

    @Test
    public void stream() {
        runTests((client, forceError, forceErrorParam) -> {
            BreezeHttpRequest request = request(client, "/putFile/{pathVariable}", forceError, forceErrorParam);
            request.put(new ByteArrayInputStream((PATH_RESULT + ", " + QUERY_ALL_RESULT + ", " + HEADER_RESULT).getBytes()));
        }, "retry:disabled");
    }

    @Test
    public void execute() {
        runTests((client, forceError, forceErrorParam) -> {
            BreezeHttpRequest request = request(client, "/get/{pathVariable}", forceError, forceErrorParam).method(GET);
            BreezeHttpResponse<TestModel> response = request.execute(TestModel.class, null);

            assertEquals(
                    PATH_RESULT + ", " + QUERY_ALL_RESULT + ", " + HEADER_RESULT,
                    response.getEntity().getMessage()
            );

            assertEquals(200, response.getHttpStatusCode());
            assertEquals("application/json;charset=UTF-8", response.getFirstHeader("content-type"));
            assertNotNull(response.getHeaders("date"));
        });
    }

    @Test
    public void form() {
        runTests((client, forceError, forceErrorParam) -> {
            TestModel model = request(client, "/form", forceError, forceErrorParam)
                    .form()
                    .param("message", "message" + MAGIC)
                    .post(TestModel.class);
            assertEquals("message" + MAGIC, model.getMessage());
        });
    }

    private interface ClientTest {
        void test(BreezeHttpClient client, String forceError, String forceErrorParam) throws Exception;
    }

    private void runTests(ClientTest test, String... testParams) {
        try {
            for (BreezeHttpClient client : clients) {
                runSuccessTest(test, client);
                runClientError(test, client);
                runServerError(test, client);
                runIOErrorTest(test, client);
                runTimeoutTest(test, client);
                runRetryTest(test, client, testParams);
            }
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("test threw " + e, e);
        }
    }

    private void runSuccessTest(ClientTest test, BreezeHttpClient client) throws Exception {
        logTest("success", client);
        test.test(client, null, null);
    }

    private void runClientError(ClientTest test, BreezeHttpClient client) throws Exception {
        runErrorTest(
                test,
                client,
                "clientError",
                "400",
                400,
                new ErrorResponse(400, "clientError")
        );
    }

    private void runServerError(ClientTest test, BreezeHttpClient client) throws Exception {
        runErrorTest(
                test,
                client,
                "serverError",
                "500",
                500,
                new ErrorResponse(500, "serverError")
        );
    }

    /**
     * Run a test where we signal the server to throw a specific type of error.
     *
     * @param test the test to run
     * @param client the client to test
     * @param forceError clientError, serverError or httpError
     * @param forceErrorParam recoverability for clientError/serverError, HTTP status code for httpError
     * @param expectedStatusCode the expected HTTP status code
     */
    private void runErrorTest(
            ClientTest test,
            BreezeHttpClient client,
            String forceError,
            String forceErrorParam,
            int expectedStatusCode,
            ErrorResponse expectedError
    ) throws Exception {
        logTest(forceError + "/" + forceErrorParam, client);

        try {
            test.test(client, forceError, forceErrorParam);
            fail("test should have thrown breeze exception");
        } catch (BreezeHttpResponseException e) {
            assertEquals("application/json;charset=UTF-8", e.getFirstHeader("content-type"));
            assertEquals("wrong HTTP status code", expectedStatusCode, e.getHttpStatusCode());

            assertNotNull(e.getFirstHeader("date"));
            assertEquals("application/json;charset=UTF-8", e.getFirstHeader("content-type"));

            ErrorResponse error = (ErrorResponse) e.getResponse().getEntity();
            assertEquals("wrong error code", expectedError.getCode(), error.getCode());
            assertEquals("wrong error message", expectedError.getMessage(), error.getMessage());
        }
    }

    /**
     * Simulate an I/O error.
     *
     * @param test the test to run
     * @param client the client to test
     */
    private void runIOErrorTest(ClientTest test, BreezeHttpClient client) throws Exception { // say args 10 times fast
        logTest("bad hostname", client);

        client = client.forService("http://localhost:1", "ioError");
        try {
            test.test(client, null, null);
            fail("client call should have thrown exception");
        } catch (BreezeHttpException e) {
            assertEquals("should not throw subclass", BreezeHttpException.class, e.getClass());
        }
    }

    private void runTimeoutTest(ClientTest test, BreezeHttpClient client) throws Exception {
        logTest("timeout", client);

        long startTime = System.currentTimeMillis();
        try {
            // Tell test controller to pause for 1 second; the clients are configured
            // to timeout after 500ms, so this should result in an BreezeHttpException
            test.test(client, "pause", "1000");
            fail("request did not timeout");
        } catch (BreezeHttpException e) {
            // Make sure the client waited at least 500ms
            assertTrue(System.currentTimeMillis() - startTime > 500);

            // Make sure it actually timed out and didn't fail for some other reason.
            // In theory different could clients throw a non-SocketTimeoutException but
            // so far all implementations have this as a root cause.
            for (Throwable t = e.getCause(); t != null; t = t.getCause()) {
                if (t instanceof SocketTimeoutException) {
                    return;
                }
            }
            fail("test failed without SocketTimeoutException");
        } finally {
            LOGGER.info("completed in {}ms", System.currentTimeMillis() - startTime);
        }
    }

    private void runRetryTest(ClientTest test, BreezeHttpClient client, String ...testParams) throws Exception {
        logTest("retry", client);

        boolean retryDisabled = (testParams != null && asList(testParams).contains("retry:disabled"));
        TestSleeper sleeper = new TestSleeper();
        List<Long> sleeps = Arrays.stream(new long[] { 100, 200 }).boxed().collect(Collectors.toList());
        TestInterceptor.externalForceErrors = asList(
                "serverError,Recoverable",
                "serverError,Recoverable"
        ).iterator();

        try {
            client = new RetryDecorator(singletonList(new AllRequestMatcher()), sleeps, sleeper).decorate(client);
            test.test(client, null, null);

            if (retryDisabled) {
                fail("expected recoverable exception");
            }
            assertFalse(TestInterceptor.externalForceErrors.hasNext());
            assertEquals(sleeps, sleeper.sleeps);
        } catch (BreezeHttpResponseException e) {
            if (retryDisabled) {
                assertEquals(500, e.getHttpStatusCode());

                ErrorResponse errorResponse = (ErrorResponse) e.getResponse().getEntity();
                assertEquals("serverError", errorResponse.getMessage());

                assertNotNull(e.getFirstHeader("date"));
                assertEquals("application/json;charset=UTF-8", e.getFirstHeader("content-type"));
            } else {
                fail("unexpected exception thrown: " + e);
            }
        } finally {
            TestInterceptor.externalForceErrors = null;
        }
    }

    /** Sleeper class that tracks sleep invocations so tests can verify behavior. */
    private static class TestSleeper extends RetryDecorator.Sleeper {
        private List<Long> sleeps = new ArrayList<>();

        @Override
        public void sleep(long milliseconds) throws InterruptedException {
            sleeps.add(milliseconds);
            Thread.sleep(milliseconds);
            if (milliseconds == -1) {
                throw new InterruptedException();
            }
        }
    }

    private void logTest(String testType, BreezeHttpClient client) {
        LOGGER.info(
                System.lineSeparator() + System.lineSeparator()
                + " ******************** Executing " + testType + " test against " + client + " ********************"
                + System.lineSeparator()
        );
    }
}
