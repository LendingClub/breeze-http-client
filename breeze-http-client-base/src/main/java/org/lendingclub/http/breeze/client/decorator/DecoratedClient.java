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

package org.lendingclub.http.breeze.client.decorator;

import org.lendingclub.http.breeze.client.BreezeHttpClient;

/**
 * A client instance that has been decorated by a decorator.
 *
 * @author Raul Acevedo
 */
public interface DecoratedClient extends BreezeHttpClient {
    /** Get the client being decorated; may be another decorator. */
    BreezeHttpClient getClient();

    /** Get the implementation class that will execute the request at the end of the decorator chain. */
    BreezeHttpClient getClientImplClass();

    /** Get the parent decorator that created this command class. */
    BreezeHttpClientDecorator getDecorator();
}
