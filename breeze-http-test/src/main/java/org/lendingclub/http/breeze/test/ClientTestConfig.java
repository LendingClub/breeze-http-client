package org.lendingclub.http.breeze.test;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

@Configuration
public class ClientTestConfig extends WebMvcConfigurerAdapter {
    /** Interceptor to handle forced errors requested from the integration test */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new TestInterceptor());
    }
}
