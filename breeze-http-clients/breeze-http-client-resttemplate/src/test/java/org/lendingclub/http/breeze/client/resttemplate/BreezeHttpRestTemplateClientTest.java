package org.lendingclub.http.breeze.client.resttemplate;

import org.junit.runner.RunWith;
import org.lendingclub.http.breeze.client.okhttp3.builder.BreezeOkHttpClient3Builder;
import org.lendingclub.http.breeze.client.resttemplate.builder.BreezeHttpComponentsRequestFactoryBuilder;
import org.lendingclub.http.breeze.client.resttemplate.builder.BreezeHttpRestTemplateClientBuilder;
import org.lendingclub.http.breeze.decorator.BreezeHttpRequestDefaultsDecorator;
import org.lendingclub.http.breeze.test.ClientIntegrationTest;
import org.lendingclub.http.breeze.test.ClientTestApplication;
import org.lendingclub.http.breeze.test.ErrorResponse;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.http.client.OkHttp3ClientHttpRequestFactory;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import static java.util.Arrays.asList;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = { ClientTestApplication.class })
@WebAppConfiguration
@IntegrationTest
public class BreezeHttpRestTemplateClientTest extends ClientIntegrationTest {
    public BreezeHttpRestTemplateClientTest() {
        super(asList(
                new BreezeHttpRestTemplateClientBuilder()
                        .decorator(new BreezeHttpRequestDefaultsDecorator(TEST_ROOT_URL))
                        .errorResponse(asList(3, 4, 5), ErrorResponse.class)
                        .requestFactory(new BreezeHttpComponentsRequestFactoryBuilder().socketTimeout(500).build())
                        .build(),
                new BreezeHttpRestTemplateClientBuilder()
                        .decorator(new BreezeHttpRequestDefaultsDecorator(TEST_ROOT_URL))
                        .errorResponse(asList(3, 4, 5), ErrorResponse.class)
                        .requestFactory(new OkHttp3ClientHttpRequestFactory(
                                new BreezeOkHttpClient3Builder().readTimeout(500).build()
                        ))
                        .build()
        ));
    }

//    static {
//        System.setProperty("logging.level.org.apache.http", "DEBUG");
//    }
}
