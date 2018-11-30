package org.lendingclub.http.breeze.client.okhttp3;

import okhttp3.OkHttpClient;
import org.junit.runner.RunWith;
import org.lendingclub.http.breeze.client.okhttp3.builder.BreezeHttpOk3ClientBuilder;
import org.lendingclub.http.breeze.client.okhttp3.builder.BreezeOkHttpClient3Builder;
import org.lendingclub.http.breeze.decorator.BreezeHttpRequestDefaultsDecorator;
import org.lendingclub.http.breeze.json.BreezeHttpGsonMapper;
import org.lendingclub.http.breeze.json.BreezeHttpJacksonMapper;
import org.lendingclub.http.breeze.json.BreezeHttpMoshiMapper;
import org.lendingclub.http.breeze.test.ClientIntegrationTest;
import org.lendingclub.http.breeze.test.ClientTestApplication;
import org.lendingclub.http.breeze.test.ErrorResponse;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import static java.util.Arrays.asList;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = { ClientTestApplication.class })
@WebAppConfiguration
@IntegrationTest
public class BreezeHttpOk3ClientTest extends ClientIntegrationTest {
    public BreezeHttpOk3ClientTest() {
        super(asList(
                new BreezeHttpOk3ClientBuilder()
                        .decorator(new BreezeHttpRequestDefaultsDecorator(TEST_ROOT_URL))
                        .errorResponse(asList(3, 4, 5), ErrorResponse.class)
                        .jsonMapper(new BreezeHttpMoshiMapper())
                        .okClient(new BreezeOkHttpClient3Builder()
                                .okClient(new OkHttpClient.Builder().build())
                                .readTimeout(500)
                                .build())
                        .build(),
                new BreezeHttpOk3ClientBuilder()
                        .decorator(new BreezeHttpRequestDefaultsDecorator(TEST_ROOT_URL))
                        .errorResponse(asList(3, 4, 5), ErrorResponse.class)
                        .jsonMapper(new BreezeHttpGsonMapper())
                        .okClient(new BreezeOkHttpClient3Builder().readTimeout(500).build())
                        .build()
                ,
                new BreezeHttpOk3ClientBuilder()
                        .decorator(new BreezeHttpRequestDefaultsDecorator(TEST_ROOT_URL))
                        .errorResponse(asList(3, 4, 5), ErrorResponse.class)
                        .jsonMapper(new BreezeHttpJacksonMapper())
                        .okClient(new BreezeOkHttpClient3Builder().readTimeout(500).build())
                        .build()
        ));
    }
}
