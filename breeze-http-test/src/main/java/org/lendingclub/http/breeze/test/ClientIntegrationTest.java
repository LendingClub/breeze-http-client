package org.lendingclub.http.breeze.test;

import org.junit.Test;
import org.lendingclub.http.breeze.BreezeHttp;
import org.lendingclub.http.breeze.BreezeHttpType;
import org.lendingclub.http.breeze.decorator.BreezeHttpRequestDefaultsDecorator;
import org.lendingclub.http.breeze.decorator.BreezeHttpRetryDecorator;
import org.lendingclub.http.breeze.exception.BreezeHttpClientErrorException;
import org.lendingclub.http.breeze.exception.BreezeHttpException;
import org.lendingclub.http.breeze.exception.BreezeHttpIOException;
import org.lendingclub.http.breeze.exception.BreezeHttpResponseException;
import org.lendingclub.http.breeze.exception.BreezeHttpServerErrorException;
import org.lendingclub.http.breeze.filter.BreezeHttpDetailLoggingFilter;
import org.lendingclub.http.breeze.filter.BreezeHttpFilter;
import org.lendingclub.http.breeze.request.BreezeHttpRequest;
import org.lendingclub.http.breeze.response.BreezeHttpRawResponse;
import org.lendingclub.http.breeze.response.BreezeHttpResponse;
import org.lendingclub.http.breeze.util.BreezeHttpUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.internal.JsonContext;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;
import static org.lendingclub.http.breeze.request.BreezeHttpRequest.Method.GET;
import static org.lendingclub.util.fluent.collections.FluentList.list;
import static org.lendingclub.util.fluent.collections.FluentMap.map;

/**
 * Integration tests that run against a real controller. For each test case, we
 * run success, error, timeout, and retry cases.
 *
 * Rather than have the backend TestController verify all inputs, it echoes back
 * whatever it received so the actual verification happens in this class. This
 * makes it easier to try different types of tests without having to touch both
 * the controller and the tests here.
 */
public class ClientIntegrationTest {
    public static final String TEST_ROOT_URL = "http://localhost:8888/test";
    public static final String MAGIC = "Magic [!@+?&]";
    public static final String PATH = "path" + MAGIC;
    public static final String QUERY = "query" + MAGIC;
    public static final String QUERY_MULTIPLE_1 = "queryMultiple1" + MAGIC;
    public static final String QUERY_MULTIPLE_2 = "queryMultiple2" + MAGIC;
    public static final String HEADER = "boom";
    public static final String PATH_RESULT = "pathVariable=" + PATH;
    public static final String QUERY_RESULT = "queryVariable=" + QUERY;
    public static final String QUERY_MULTIPLE_RESULT = "queryMultiple=[" + QUERY_MULTIPLE_1
            + ", " + QUERY_MULTIPLE_2
            + "]";
    public static final String QUERY_ALL_RESULT = QUERY_RESULT + ", " + QUERY_MULTIPLE_RESULT;
    public static final String HEADER_RESULT = "header=" + HEADER;
    protected final Logger logger = LoggerFactory.getLogger(getClass().getSimpleName());
    protected List<BreezeHttp> clients = new ArrayList<>();

    public ClientIntegrationTest(List<BreezeHttp> clients) {
        this.clients.addAll(clients);
    }

    public BreezeHttpRequest request(BreezeHttp client, String path, String forceError, String forceErrorParam) {
        return client.request()
                .service("ClientIntegrationTest")
                .name(Thread.currentThread().getStackTrace()[5].getMethodName()) // yeah i know
                .path(path)
                .pathVariable("pathVariable", PATH)
                .queryParameter("queryVariable", QUERY)
                .queryParameter("queryMultiple", QUERY_MULTIPLE_1)
                .queryParameter("queryMultiple", QUERY_MULTIPLE_2)
                .header("TestHeader", HEADER)
                .queryParameter("forceError", forceError)
                .queryParameter("forceErrorParam", forceErrorParam);
    }

    @Test
    public void get() {
        runTests((client, forceError, forceErrorParam) -> {
            String result = request(client, "/get/{pathVariable}", forceError, forceErrorParam).get();

            assertTrue(result.contains(PATH_RESULT));
            assertTrue(result.contains(QUERY_RESULT));
            assertTrue(result.contains(QUERY_MULTIPLE_RESULT));
            assertTrue(result.contains(HEADER_RESULT));
        });
    }

    @Test
    public void gzip() {
        runTests((client, forceError, forceErrorParam) -> {
            String result = request(client, "/get/{pathVariable}", forceError, forceErrorParam)
                    .gzip()
                    .execute(GET, null, String.class);
            assertEquals("gzip", TestInterceptor.responseEncoding);
            assertTrue(result.contains(PATH_RESULT));
            assertTrue(result.contains(QUERY_RESULT));
            assertTrue(result.contains(QUERY_MULTIPLE_RESULT));
            assertTrue(result.contains(HEADER_RESULT));
        });
    }

    @Test
    public void nullGet() {
        runTests((client, forceError, forceErrorParam) ->
                assertNull(request(client, "/null", forceError, forceErrorParam).get(Void.class))
        );
    }

    @Test
    public void voidGet() {
        runTests((client, forceError, forceErrorParam) ->
                assertNull(request(client, "/void", forceError, forceErrorParam).get(Void.class))
        );
    }

    @Test
    public void getObject() {
        runTests((client, forceError, forceErrorParam) ->
                assertEquals(
                        PATH_RESULT + ", " + QUERY_ALL_RESULT + ", " + HEADER_RESULT,
                        request(client, "/get/{pathVariable}", forceError, forceErrorParam).get(TestModel.class).getMessage()
                )
        );
    }

    @Test
    public void getMap() {
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
    public void postObject() {
        runTests((client, forceError, forceErrorParam) -> {
            BreezeHttpRequest request = request(
                    client,
                    "/post/{pathVariable}",
                    forceError,
                    forceErrorParam
            );
            TestModel model = request.post("this is a post", TestModel.class);

            assertEquals(
                    PATH_RESULT + ", " + QUERY_ALL_RESULT + ", " + HEADER_RESULT + ", body=this is a post",
                    model.getMessage()
            );
        });
    }

    @Test
    public void postMap() {
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
                    "this is a post",
                    new BreezeHttpType<Map<String, List<TestModel>>>() {}
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
    public void putObject() {
        runTests((client, forceError, forceErrorParam) -> {
            BreezeHttpRequest request = request(client, "/put/{pathVariable}", forceError, forceErrorParam);

            assertEquals(
                    PATH_RESULT + ", " + QUERY_ALL_RESULT + ", " + HEADER_RESULT + ", body=this is a put",
                    request.put("this is a put", TestModel.class).getMessage()
            );
        });
    }

    @Test
    public void putMap() {
        runTests((client, forceError, forceErrorParam) -> {
            Map<String, List<TestModel>> expected = Collections.singletonMap(
                    PATH,
                    singletonList(new TestModel(QUERY_ALL_RESULT + ", " + HEADER_RESULT + ", body=this is a put"))
            );

            BreezeHttpRequest request = request(client, "/putMap/{pathVariable}", forceError, forceErrorParam);
            Map<String, List<TestModel>> result = request.put(
                    "this is a put",
                    new BreezeHttpType<Map<String, List<TestModel>>>() {}
            );

            assertEquals(expected, result);
        });
    }

    @Test
    public void putStream() {
        runTests((client, forceError, forceErrorParam) -> {
            BreezeHttpRequest request = request(client, "/putFile/{pathVariable}", forceError, forceErrorParam);
            request.put(new ByteArrayInputStream(
                    (PATH_RESULT + ", " + QUERY_ALL_RESULT + ", " + HEADER_RESULT).getBytes()
            ));

        }, "retryDisabled");
    }

    @Test
    public void patch() {
        runTests((client, forceError, forceErrorParam) ->
                request(client, "/patch", forceError, forceErrorParam).patch(new TestModel("message" + MAGIC))
        );
    }

    @Test
    public void patchObject() {
        runTests((client, forceError, forceErrorParam) -> {
            TestModel model = request(client, "/patch", forceError, forceErrorParam)
                    .patch(new TestModel("message" + MAGIC), TestModel.class);
            assertEquals("message" + MAGIC, model.getMessage());
        });
    }

    @Test
    public void patchMap() {
        runTests((client, forceError, forceErrorParam) -> {
            Map<String, List<TestModel>> expected = Collections.singletonMap(
                    PATH,
                    singletonList(new TestModel(QUERY_ALL_RESULT + ", " + HEADER_RESULT + ", body=this is a patch"))
            );

            BreezeHttpRequest request = request(client, "/patchMap/{pathVariable}", forceError, forceErrorParam);
            Map<String, List<TestModel>> result = request.patch(
                    "this is a patch",
                    new BreezeHttpType<Map<String, List<TestModel>>>() {}
            );

            assertEquals(expected, result);
        });
    }

    @Test
    public void execute() {
        runTests((client, forceError, forceErrorParam) -> {
            BreezeHttpRequest request = request(client, "/get/{pathVariable}", forceError, forceErrorParam).method(GET);
            BreezeHttpResponse<TestModel> response = request.execute(
                    null,
                    new BreezeHttpType<BreezeHttpResponse<TestModel>>() {}
            );

            assertEquals(
                    PATH_RESULT + ", " + QUERY_ALL_RESULT + ", " + HEADER_RESULT,
                    response.body().getMessage()
            );

            assertEquals(200, response.httpStatus());
            assertEquals("application/json;charset=UTF-8", response.header("content-type"));
            assertNotNull(response.headers("date"));
        });
    }

    @Test
    public void getBreezeResponse() {
        runTests((client, forceError, forceErrorParam) -> {
            Map<String, List<TestModel>> expected = Collections.singletonMap(
                    PATH,
                    singletonList(new TestModel(QUERY_ALL_RESULT + ", " + HEADER_RESULT))
            );

            BreezeHttpRequest request = request(client, "/getMap/{pathVariable}", forceError, forceErrorParam);
            BreezeHttpResponse<Map<String, List<TestModel>>> response =
                    request.get(new BreezeHttpType<BreezeHttpResponse<Map<String, List<TestModel>>>>() {});

            assertEquals(expected, response.body());
            assertEquals(200, response.httpStatus());
            assertEquals("application/json;charset=UTF-8", response.header("content-type"));
            assertNotNull(response.headers("date"));
        });
    }

    @Test
    public void getBreezeRawResponse() {
        runTests((client, forceError, forceErrorParam) -> {
            Map<String, List<TestModel>> expected = Collections.singletonMap(
                    PATH,
                    singletonList(new TestModel(QUERY_ALL_RESULT + ", " + HEADER_RESULT))
            );

            BreezeHttpRequest request = request(client, "/getMap/{pathVariable}", forceError, forceErrorParam);
            BreezeHttpRawResponse response = request.get(BreezeHttpRawResponse.class);

            assertEquals("application/json;charset=UTF-8", response.header("content-type"));
            assertNotNull(response.headers("date"));

            if (response.isSuccess()) {
                assertEquals(200, response.httpStatus());
                assertEquals(expected, response.convert(new BreezeHttpType<Map<String, List<TestModel>>>() {}.type()));
            } else if (response.isError()) {
                ErrorResponse errorResponse = response.convert(ErrorResponse.class);
                assertEquals(response.httpStatus(), errorResponse.getCode());
                assertEquals(response.isClientError() ? "clientError" : "serverError", errorResponse.getMessage());
            }

            try {
                response.convert(new BreezeHttpType<Map<String, List<TestModel>>>() {}.type());
                fail("should not be able to read input stream twice");
            } catch (Exception e) {
                // expected
            }
        }, "noDetailLogging", "httpErrorsSucceed", "retryDisabled");
    }

    @Test
    public void executeBreezeRawResponse() {
        runTests((client, forceError, forceErrorParam) -> {
            Map<String, List<TestModel>> expected = Collections.singletonMap(
                    PATH,
                    singletonList(new TestModel(QUERY_ALL_RESULT + ", " + HEADER_RESULT))
            );

            BreezeHttpRequest request = request(client, "/getMap/{pathVariable}", forceError, forceErrorParam);
            BreezeHttpRawResponse raw = request.method(GET).execute(null, BreezeHttpRawResponse.class);

            assertEquals("application/json;charset=UTF-8", raw.header("content-type"));
            assertNotNull(raw.headers("date"));

            if (raw.isSuccess()) {
                assertEquals(200, raw.httpStatus());
                assertEquals(expected, raw.convert(new BreezeHttpType<Map<String, List<TestModel>>>() {}.type()));
            } else if (raw.isError()) {
                ErrorResponse errorResponse = raw.convert(ErrorResponse.class);
                assertEquals(raw.httpStatus(), errorResponse.getCode());
                assertEquals(raw.isClientError() ? "clientError" : "serverError", errorResponse.getMessage());
            }

            try {
                raw.convert(new BreezeHttpType<Map<String, List<TestModel>>>() {}.type());
                fail("should not be able to read input stream twice");
            } catch (Exception e) {
                // expected
            }
        }, "noDetailLogging", "httpErrorsSucceed", "retryDisabled");
    }

    @Test
    public void responseStream() {
        runTests((client, forceError, forceErrorParam) -> {
            BreezeHttpRequest request = request(client, "/get/{pathVariable}", forceError, forceErrorParam);
            try (InputStream inputStream = request.get(InputStream.class)) {
                String result = BreezeHttpUtil.readString(inputStream);
                assertEquals(0, inputStream.available());
                assertTrue(result.contains(PATH_RESULT));
                assertTrue(result.contains(QUERY_RESULT));
                assertTrue(result.contains(QUERY_MULTIPLE_RESULT));
                assertTrue(result.contains(HEADER_RESULT));
            }
        });
    }

    @Test
    public void readResponseOnce() {
        for (BreezeHttp client : clients) {
            try {
                client.filters().add(new BreezeHttpFilter() {
                    @Override
                    public boolean executed(BreezeHttpRequest request, BreezeHttpRawResponse raw) {
                        logger.info("read raw response body=\"" + raw.string() + "\"");
                        return true;
                    }
                });
                BreezeHttpRequest request = request(client, "/get/{pathVariable}", null, null).method(GET);
                request.execute(null, TestModel.class);
                fail("response can only be read once");
            } catch (BreezeHttpException e) {
                logger.info("correctly failed with " + e);
            } finally {
                client.filters().clear();
            }
        }
    }

    @Test
    public void bufferResponse() {
        runTests((client, forceError, forceErrorParam) -> {
            try {
                client.filters().add(new BreezeHttpFilter() {
                    @Override
                    public boolean executed(BreezeHttpRequest request, BreezeHttpRawResponse raw) {
                        assertNotNull(raw.string());
                        assertEquals(raw.string(), raw.string());
                        logger.info("read raw response multiple times, body=\"" + raw.string() + "\"");
                        return true;
                    }
                });
                String result = request(client, "/get/{pathVariable}", forceError, forceErrorParam)
                        .bufferResponse(true)
                        .get();
                assertTrue(result.contains(PATH_RESULT));
                assertTrue(result.contains(QUERY_RESULT));
                assertTrue(result.contains(QUERY_MULTIPLE_RESULT));
                assertTrue(result.contains(HEADER_RESULT));
            } finally {
                client.filters().clear();
            }
        });
    }

    @Test
    public void fluentPost() {
        runTests((client, forceError, forceErrorParam) -> {
            TestModel model = request(client, "/json/post", forceError, forceErrorParam)
                    .map()
                    .entry("message", "this is the message")
                    .entry("map", map("key", list("one", "two")))
                    .post(TestModel.class);

            assertEquals("this is the message", model.getMessage());
            assertEquals(asList("one", "two"), model.getMap().get("key"));
        });
    }

    @Test
    public void fluentPut() {
        runTests((client, forceError, forceErrorParam) -> {
            TestModel model = request(client, "/json/put", forceError, forceErrorParam)
                    .map()
                    .entry("message", "this is the message")
                    .entry("map", map("key", list("one", "two")))
                    .put(TestModel.class);

            assertEquals("this is the message", model.getMessage());
            assertEquals(asList("one", "two"), model.getMap().get("key"));
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

    @Test
    public void multipart() {
        runTests((client, forceError, forceErrorParam) -> {
            List<TestMultipartResult> multiparts = request(client, "/multipart", forceError, forceErrorParam)
                    .multipart()
                    .part("hi", "text/plain", new ClassPathResource("hi.txt").getFile())
                    .part("pr", "image/png", new ClassPathResource("pr.png").getInputStream())
                    .part("name", "raul")
                    .part("city", "san francisco")
                    .post(new BreezeHttpType<List<TestMultipartResult>>() {});
            assertEquals(4, multiparts.size());

            TestMultipartResult part = multiparts.get(0);
            assertEquals("hi", part.name);
            assertEquals("hi.txt", part.filename);
            assertEquals(3, part.size);
            assertEquals("text/plain", part.contentType);
            assertTrue(part.headers.get("content-disposition").contains("form-data"));

            part = multiparts.get(1);
            assertEquals("pr", part.name);
            assertEquals(6685, part.size);
            assertEquals("image/png", part.contentType);
            assertTrue(part.headers.get("content-disposition").contains("form-data"));

            part = multiparts.get(2);
            assertEquals("name", part.name);
            assertEquals(4, part.size);
            assertTrue(part.contentType.startsWith("text/plain"));
            assertTrue(part.headers.get("content-disposition").contains("form-data"));

            part = multiparts.get(3);
            assertEquals("city", part.name);
            assertEquals(13, part.size);
            assertTrue(part.contentType.startsWith("text/plain"));
            assertTrue(part.headers.get("content-disposition").contains("form-data"));
        }, "retryDisabled");
    }

    @Test
    public void gson() {
        runTests((client, forceError, forceErrorParam) -> {
            String json = "{\"message\":\"this is the message\",\"map\":{\"key\":[\"one\",\"two\"]},\"objects\":null}";
            JsonElement element = new JsonParser().parse(json);
            JsonObject response = request(client, "/json/put", forceError, forceErrorParam).put(
                    element,
                    JsonObject.class
            );

            assertEquals(element, response);
        });
    }

    @Test
    public void jackson() {
        runTests((client, forceError, forceErrorParam) -> {
            String json = "{\"message\":\"this is the message\",\"map\":{\"key\":[\"one\",\"two\"]},\"objects\":null}";
            JsonNode node = new ObjectMapper().readTree(json);
            ObjectNode response = request(client, "/json/put", forceError, forceErrorParam).put(
                    node,
                    ObjectNode.class
            );

            assertEquals(node, response);
        });
    }

    @Test
    public void jsonpath() {
        runTests((client, forceError, forceErrorParam) -> {
            String json = "{\"message\":\"this is the message\",\"map\":{\"key\":[\"one\",\"two\"]},\"objects\":null}";
            DocumentContext document = JsonPath.parse(json);
            JsonContext response = request(client, "/json/put", forceError, forceErrorParam).put(
                    document,
                    JsonContext.class
            );

            assertEquals("this is the message", response.read("$.message"));
            assertEquals("one", response.read("$.map.key[0]"));
            assertEquals("two", response.read("$.map.key[1]"));
        });
    }

    @Test
    public void putJsonString() {
        runTests((client, forceError, forceErrorParam) -> {
            String json = "{\"message\":\"this is the message\",\"map\":{\"key\":[\"one\",\"two\"]},\"objects\":null}";
            TestModel model = request(client, "/json/put", forceError, forceErrorParam).put(json, TestModel.class);

            assertEquals("this is the message", model.getMessage());
            assertEquals(asList("one", "two"), model.getMap().get("key"));
        });
    }

    protected void runTests(ClientTest test, String... testParams) {
        for (BreezeHttp client : clients) {
            try {
                List<String> params = testParams == null || testParams.length == 0 ? emptyList() : asList(testParams);
                if (!params.contains("noDetailLogging")) {
                    client.filters().add(new BreezeHttpDetailLoggingFilter());
                }

                runSuccessTest(test, client);
                runErrorTest(test, client, "httpError", 300, BreezeHttpResponseException.class, params);
                runErrorTest(test, client, "clientError", 400, BreezeHttpClientErrorException.class, params);
                runErrorTest(test, client, "serverError", 500, BreezeHttpServerErrorException.class, params);
                runIOErrorTest(test, client);
                runTimeoutTest(test, client);
                runRetryTest(test, client, params);
            } catch (RuntimeException e) {
                throw e;
            } catch (Exception e) {
                throw new RuntimeException("test threw " + e, e);
            } finally {
                client.filters().clear();
            }
        }
    }

    protected void runSuccessTest(ClientTest test, BreezeHttp client) throws Exception {
        logTest("success", client);
        test.test(client, null, null);
    }

    /**
     * Run a test where we signal the server to throw a specific type of error.
     */
    protected void runErrorTest(
            ClientTest test,
            BreezeHttp client,
            String forceError,
            int httpStatus,
            Class<? extends BreezeHttpResponseException> exceptionClass,
            List<String> params
    ) throws Exception {
        logTest(forceError + "/" + httpStatus, client);

        try {
            test.test(client, forceError, String.valueOf(httpStatus));
            if (!params.contains("httpErrorsSucceed")) {
                fail("test should have thrown BreezeHttpResponseException");
            }
        } catch (BreezeHttpResponseException e) {
            assertEquals("application/json;charset=UTF-8", e.header("content-type"));
            assertEquals(httpStatus, e.httpStatus());
            assertNotNull(e.header("date"));
            ErrorResponse errorResponse = e.body();
            assertEquals(httpStatus, errorResponse.getCode());
            assertEquals(forceError, errorResponse.getMessage());
            assertEquals(exceptionClass, e.getClass());
        }
    }

    /**
     * Simulate an I/O error. I/O errors (e.g. connection refused) mean that we
     * weren't able to communicate with the server, so there is no server response or
     * even HTTP status code possible. The client should throw BreezeHttpIOException.
     *
     * @param test the test to run
     * @param client the client to test
     */
    protected void runIOErrorTest(ClientTest test, BreezeHttp client) throws Exception {
        logTest("bad hostname", client);

        client = client.decorate(new BreezeHttpRequestDefaultsDecorator("http://localhost:1", "ioError"));
        try {
            test.test(client, null, null);
            fail("client call should have thrown exception");
        } catch (BreezeHttpIOException e) {
            assertNotNull(BreezeHttpIOException.findIOExceptionCause(e));
        }
    }

    protected void runTimeoutTest(ClientTest test, BreezeHttp client) {
        logTest("timeout", client);

        long startTime = System.currentTimeMillis();
        try {
            // Tell test controller to pause for 1 second; the clients are configured
            // to timeout after 500ms; we expect BreezeHttpIOException with a cause,
            // somewhere down the stack, of SocketTimeoutException.
            test.test(client, "pause", "1000");
            fail("request did not timeout");
        } catch (Exception e) {
            // IOException should be wrapped in our own BreezeHttpIOException
            assertEquals(BreezeHttpIOException.class, e.getClass());

            // Make sure the client waited at least 500ms or thereabouts
            long delay = System.currentTimeMillis() - startTime;
            assertTrue("delay " + delay + " ms was not long enough", delay > 500);

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
            logger.info("completed in {} ms", System.currentTimeMillis() - startTime);
        }
    }

    protected void runRetryTest(ClientTest test, BreezeHttp client, List<String> testParams) throws Exception {
        if (testParams.contains("retryDisabled")) {
            return;
        }

        logTest("retry", client);
        TestSleeper sleeper = new TestSleeper();
        List<Long> sleeps = asList(100L, 200L);
        TestInterceptor.externalForceErrors = asList("serverError", "serverError").iterator();

        try {
            client = client.decorate(new BreezeHttpRetryDecorator(
                    request -> true,
                    sleeps,
                    new BreezeHttpRetryDecorator.DefaultRetryableTest(),
                    sleeper
            ));
            test.test(client, null, null);
            assertFalse(TestInterceptor.externalForceErrors.hasNext());
            assertEquals(sleeps, sleeper.sleeps);
        } catch (BreezeHttpResponseException e) {
            fail("unexpected exception thrown: " + e);
        } finally {
            TestInterceptor.externalForceErrors = null;
        }
    }

    protected void logTest(String testType, BreezeHttp client) {
        String info = "******************** Executing "
                + Thread.currentThread().getStackTrace()[4].getMethodName()
                + " " + testType + " test against " + client
                + " ********************";
        String stars = new String(new char[info.length()]).replace("\0", "*");
        logger.info(System.lineSeparator() + System.lineSeparator()
                + stars + System.lineSeparator()
                + info + System.lineSeparator()
                + stars + System.lineSeparator());
    }

    protected interface ClientTest {
        void test(BreezeHttp client, String forceError, String forceErrorParam) throws Exception;
    }

    /** Sleeper class that tracks sleep invocations so tests can verify behavior. */
    protected static class TestSleeper extends BreezeHttpRetryDecorator.Sleeper {
        private List<Long> sleeps = new ArrayList<>();

        @Override
        public void sleep(long milliseconds) {
            sleeps.add(milliseconds);
            try {
                Thread.sleep(milliseconds);
                if (milliseconds == -1) {
                    throw new InterruptedException();
                }
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
