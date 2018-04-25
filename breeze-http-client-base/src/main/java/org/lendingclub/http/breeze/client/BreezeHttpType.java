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

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * Shamelessly copied from Spring's ParameterizedTypeReference. Accomplishes
 * the same thing as that class and JAX-RS GenericType: the ability to create
 * class references to Java generics, based on the fact that while Java
 * generics lose type information at runtime, that type information is
 * preserved for subclasses.
 *
 * This class allows BreezeHttp to use type-safe generics across
 * RestTemplate, JAX-RS and hopefully other implementations that support JSON
 * conversion via Java reflection's Type class.
 *
 * @author Raul Acevedo
 */
public class BreezeHttpType<T> {
    private final Type type;

    protected BreezeHttpType() {
        Class<?> subclass = findGenericSubclass(getClass());
        Type type = subclass.getGenericSuperclass();
        ParameterizedType parameterizedType = (ParameterizedType) type;
        this.type = parameterizedType.getActualTypeArguments()[0];
    }

    public BreezeHttpType(Type type) {
        this.type = type;
    }

    public Type getType() {
        return this.type;
    }

    private static Class<?> findGenericSubclass(Class<?> child) {
        Class<?> parent = child.getSuperclass();
        if (Object.class == parent) {
            throw new IllegalStateException("expected BreezeHttpType superclass");
        } else if (BreezeHttpType.class == parent) {
            return child;
        } else {
            return findGenericSubclass(parent);
        }
    }
}
