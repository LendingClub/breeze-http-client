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

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.embedded.EmbeddedServletContainerFactory;
import org.springframework.boot.context.embedded.tomcat.TomcatEmbeddedServletContainerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;

import org.lendingclub.http.breeze.client.decorator.retry.config.BreezeHttpClientRetryDecoratorConfig;

/**
 * Spring Boot application class for test server.
 *
 * @author Raul Acevedo
 */
@SpringBootApplication
@ComponentScan(
        basePackages = {"org.lendingclub.http.breeze.client"},
        // Exclude retry and circuit breaker because they will mess up the error tests
        excludeFilters = {
                @ComponentScan.Filter(
                        value = BreezeHttpClientRetryDecoratorConfig.class,
                        type = FilterType.ASSIGNABLE_TYPE
                )
        }
)
public class ClientTestApplication {
    public static void main(String[] args) {
        SpringApplication.run(ClientTestApplication.class, args);
    }

    @Bean
    public EmbeddedServletContainerFactory servletContainer() {
        return new TomcatEmbeddedServletContainerFactory(8888);
    }
}
