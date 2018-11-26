package org.lendingclub.http.breeze.client.resttemplate;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.lendingclub.http.breeze.client.resttemplate.builder.BreezeHttpRestTemplateClientBuilder;
import org.lendingclub.http.breeze.client.resttemplate.builder.BreezeHttpSimpleRequestFactoryBuilder;
import org.lendingclub.http.breeze.decorator.BreezeHttpRequestDefaultsDecorator;
import org.lendingclub.http.breeze.test.ClientIntegrationTest;
import org.lendingclub.http.breeze.test.ClientTestApplication;
import org.lendingclub.http.breeze.test.ErrorResponse;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = { ClientTestApplication.class })
@WebAppConfiguration
@IntegrationTest
public class BreezeHttpRestTemplateSimpleFactoryClientTest extends ClientIntegrationTest {
    public BreezeHttpRestTemplateSimpleFactoryClientTest() {
        super(singletonList(
                new BreezeHttpRestTemplateClientBuilder()
                        .decorator(new BreezeHttpRequestDefaultsDecorator(TEST_ROOT_URL))
                        .errorResponse(asList(3, 4, 5), ErrorResponse.class)
                        .requestFactory(new BreezeHttpSimpleRequestFactoryBuilder().readTimeout(500).build())
                        .build()
        ));
    }

    @Test
    public void gzip() {
        // not supported
    }

    @Test
    public void patch() {
        // not supported
    }

    @Test
    public void patchObject() {
        // not supported
    }

    @Test
    public void patchMap() {
        // not supported
    }
}
