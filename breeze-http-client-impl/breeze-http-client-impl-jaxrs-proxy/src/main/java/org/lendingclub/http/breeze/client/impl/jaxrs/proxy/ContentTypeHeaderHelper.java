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

import java.lang.reflect.Method;

import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;

/**
 * This class determines what the Content-Type header or Accept header values will be.
 * @author salbin
 */
public class ContentTypeHeaderHelper {

    /**
     * Whatever the service Produces is what we Accept.
     * @param proxyInterfaceClass
     * @param method
     * @return
     */
    public String getAcceptsHeader(final Class<?> proxyInterfaceClass, final Method method) {
        String produces = null;

        Produces methodLevelProduces = method.getAnnotation(Produces.class);
        if (methodLevelProduces != null && methodLevelProduces.value().length > 0) {
            produces = methodLevelProduces.value()[0];
        } else {
            Produces classLevelProduces = proxyInterfaceClass.getAnnotation(Produces.class);

            if (classLevelProduces != null && classLevelProduces.value().length > 0) {
                produces = classLevelProduces.value()[0];
            }
        }

        return produces;
    }

    /**
     * Whatever the service 'consumes' is what we have to send.
     * @param proxyInterfaceClass
     * @param method
     * @return content type from method or class if found, null otherwise.
     */
    public String getContentTypeHeader(final Class<?> proxyInterfaceClass, final Method method) {
        String contentType = null;

        // First check the method
        Consumes methodLevelConsumes = method.getAnnotation(Consumes.class);
        if (methodLevelConsumes != null && methodLevelConsumes.value().length > 0) {
            contentType = methodLevelConsumes.value()[0];
        } else {
            Consumes classLevelConsumes = proxyInterfaceClass.getAnnotation(Consumes.class);

            if (classLevelConsumes != null && classLevelConsumes.value().length > 0) {
                contentType = classLevelConsumes.value()[0];
            }
        }

        return contentType;
    }
}
