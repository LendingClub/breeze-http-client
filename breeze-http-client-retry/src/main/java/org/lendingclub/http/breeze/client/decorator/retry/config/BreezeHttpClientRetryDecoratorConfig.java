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

package org.lendingclub.http.breeze.client.decorator.retry.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

import org.lendingclub.http.breeze.client.decorator.BreezeHttpClientDecorator;
import org.lendingclub.http.breeze.client.decorator.retry.RetryDecorator;

/**
 * Simple retry configuration.
 *
 * @author Raul Acevedo
 */
@Configuration
public class BreezeHttpClientRetryDecoratorConfig {
    @Bean
    public BreezeHttpClientDecorator breezeHttpClientRetryDecorator(
            @Value(value = "${BREEZE_HTTP_RESTTEMPLATE_CLIENT_RETRY_DECORATOR_SLEEPS:100,500,1000}") String sleeps
    ) {
        List<Long> sleepsList = new ArrayList<>();
        for (String sleep : sleeps.split(",")) {
            sleepsList.add(Long.parseLong(sleep));
        }
        return new RetryDecorator(sleepsList);
    }
}
