package org.lendingclub.http.breeze.client.jaxrs.jersey;

import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.ClientProperties;
import org.glassfish.jersey.client.HttpUrlConnectorProvider;
import org.glassfish.jersey.client.JerseyClientBuilder;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.message.GZipEncoder;
import org.junit.runner.RunWith;
import org.lendingclub.http.breeze.decorator.BreezeHttpRequestDefaultsDecorator;
import org.lendingclub.http.breeze.test.ClientIntegrationTest;
import org.lendingclub.http.breeze.test.ClientTestApplication;
import org.lendingclub.http.breeze.test.ErrorResponse;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import com.fasterxml.jackson.jaxrs.json.JacksonJaxbJsonProvider;

import static com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = { ClientTestApplication.class })
@WebAppConfiguration
@IntegrationTest
public class BreezeHttpJaxRsJerseyClientTest extends ClientIntegrationTest {
    public BreezeHttpJaxRsJerseyClientTest() {
        super(singletonList(
                new BreezeHttpJaxRsJerseyClientBuilder()
                        .decorator(new BreezeHttpRequestDefaultsDecorator(TEST_ROOT_URL))
                        .errorResponse(asList(3, 4, 5), ErrorResponse.class)
                        .client(JerseyClientBuilder.createClient(
                                new ClientConfig(
                                        // jackson sucks
                                        new JacksonJaxbJsonProvider().configure(FAIL_ON_UNKNOWN_PROPERTIES, false))
                                )
                                .register(GZipEncoder.class)
                                .register(MultiPartFeature.class)
                                .property(ClientProperties.CONNECT_TIMEOUT, 500)
                                .property(ClientProperties.READ_TIMEOUT, 500)
                                .property(HttpUrlConnectorProvider.SET_METHOD_WORKAROUND, true) // For HTTP PATCH
                        ).build()
        ));
    }
}
