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

package org.lendingclub.http.breeze.client.impl.jaxrs.proxy;

import org.junit.Test;

import javax.ws.rs.core.Form;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import org.lendingclub.http.breeze.client.BreezeHttpClient;
import org.lendingclub.http.breeze.client.BreezeHttpRequest;
import org.lendingclub.http.breeze.client.exception.BreezeHttpException;

import static junit.framework.TestCase.assertFalse;
import static junit.framework.TestCase.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * 
 * @author salbin
 *
 */
public class BreezeWebResourceFactoryTest {

    @Test
    public void testProducesOctetStream() {
        BreezeHttpClient breezeHttpClient = mock(BreezeHttpClient.class);

        BreezeProxyInterfaceClientFactory clientFactory = new BreezeProxyInterfaceClientFactory(breezeHttpClient);

        MockService mockService = clientFactory.createProxyInterfaceClient(MockService.class, new HashMap<>(),
                "http://dummy");

        mockService.getOctetStream();
    }

    @Test
    public void testPostForm() {
        BreezeHttpClient breezeHttpClient = mock(BreezeHttpClient.class);

        BreezeProxyInterfaceClientFactory clientFactory = new BreezeProxyInterfaceClientFactory(breezeHttpClient);

        MockService mockService = clientFactory.createProxyInterfaceClient(MockService.class, new HashMap<>(),
                "http://dummy");

        mockService.postForm(new Form());
    }

    @Test
    public void testInvoke() {
        BreezeHttpClient breezeHttpClient = mock(BreezeHttpClient.class);

        BreezeProxyInterfaceClientFactory clientFactory = new BreezeProxyInterfaceClientFactory(breezeHttpClient);

        MockService mockService = clientFactory.createProxyInterfaceClient(MockService.class, new HashMap<>(),
                "http://dummy");

        // For code coverage
        mockService.toString();
        assertFalse(mockService.equals(null));
        assertTrue(mockService.equals(mockService));
        mockService.hashCode();

        mockService.noPath();

        mockService.getStatus();

        mockService.getBarfu("barfu", "fubar", "test", null, null, "param1", "param2");

        mockService.fubarPost(new ArrayList<>());
        mockService.fubarPost(Arrays.asList("test"));

        mockService.newBarfu(new MockRequest());
        mockService.fubarPostWithIncorrectAnnotation("test");
    }

    @Test
    public void testEmptyConsumesAndProduces() {
        BreezeHttpClient breezeHttpClient = mock(BreezeHttpClient.class);

        BreezeProxyInterfaceClientFactory clientFactory = new BreezeProxyInterfaceClientFactory(breezeHttpClient);

        MockService mockService = clientFactory.createProxyInterfaceClient(MockService.class, null, "http://dummy");
        mockService.whatDoIConsumeAndProduce(new MockRequest());

    }

    @Test
    public void testNoHeaders() {
        BreezeHttpClient breezeHttpClient = mock(BreezeHttpClient.class);

        BreezeProxyInterfaceClientFactory clientFactory = new BreezeProxyInterfaceClientFactory(breezeHttpClient);

        MockService mockService = clientFactory.createProxyInterfaceClient(MockService.class, null, "http://dummy");

        mockService.getStatus();
    }

    @Test
    public void testInterfaceAnnotations() {
        BreezeHttpClient breezeHttpClient = mock(BreezeHttpClient.class);

        BreezeProxyInterfaceClientFactory clientFactory = new BreezeProxyInterfaceClientFactory(breezeHttpClient);

        MockService2 mockService = clientFactory.createProxyInterfaceClient(MockService2.class, null, "http://dummy");

        mockService.getBarfu2();
        mockService.getBarfuVoidResponse(new MockRequest());
        mockService.getBarfuCollectionResponse();
        mockService.putBarfu(new MockRequest());
        mockService.putBarfuVoidResponse(new MockRequest());
        mockService.putBarfu3(new MockRequest());
        mockService.postBarfu(new MockRequest());
        mockService.postBarfuVoidResponse(new MockRequest());
        mockService.postBarfu3(new MockRequest());
        mockService.postBarfu4(new Form());
        mockService.postBarfu4(new Form().param("param", "value").param("coll", "a").param("coll", "b"));
    }

    @Test
    public void testEmptyInterfaceAnnotations() {
        BreezeHttpClient breezeHttpClient = mock(BreezeHttpClient.class);

        BreezeProxyInterfaceClientFactory clientFactory = new BreezeProxyInterfaceClientFactory(breezeHttpClient);

        MockService3 mockService = clientFactory.createProxyInterfaceClient(MockService3.class, null, "http://dummy");

        mockService.getStuff("");
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testResponse() {
        BreezeHttpClient breezeHttpClient = mock(BreezeHttpClient.class);

        when(breezeHttpClient.get(any(BreezeHttpRequest.class), any(Class.class))).thenReturn(new MockResponse());

        BreezeProxyInterfaceClientFactory clientFactory = new BreezeProxyInterfaceClientFactory(breezeHttpClient);

        MockService mockService = clientFactory.createProxyInterfaceClient(MockService.class, null, () -> "http://dummy");

        mockService.getStatus();
    }

    @Test(expected = BreezeHttpException.class)
    public void testNoHttpMethod() {
        BreezeHttpClient breezeHttpClient = mock(BreezeHttpClient.class);

        BreezeProxyInterfaceClientFactory clientFactory = new BreezeProxyInterfaceClientFactory(breezeHttpClient);

        MockService mockService = clientFactory.createProxyInterfaceClient(MockService.class, null, "http://dummy");

        mockService.noHttpMethod();
    }
}
