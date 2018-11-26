package org.lendingclub.http.breeze.test;

import org.apache.catalina.filters.RequestDumperFilter;
import org.apache.coyote.http11.AbstractHttp11Protocol;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.embedded.EmbeddedServletContainerCustomizer;
import org.springframework.boot.context.embedded.EmbeddedServletContainerFactory;
import org.springframework.boot.context.embedded.FilterRegistrationBean;
import org.springframework.boot.context.embedded.tomcat.TomcatConnectorCustomizer;
import org.springframework.boot.context.embedded.tomcat.TomcatEmbeddedServletContainerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.MediaType;
import org.springframework.web.filter.CommonsRequestLoggingFilter;

import javax.servlet.Filter;
import java.io.IOException;

@SpringBootApplication
@ComponentScan(basePackages = { "org.lendingclub.http.breeze.test*" })
public class ClientTestApplication {
    public static void main(String[] args) {
        SpringApplication.run(ClientTestApplication.class, args);
    }

    @Bean
    public EmbeddedServletContainerFactory servletContainer() throws IOException {
        return new TomcatEmbeddedServletContainerFactory(8888);
    }

    @Bean
    @SuppressWarnings("rawtypes")
    public EmbeddedServletContainerCustomizer servletContainerCustomizer() {
        return factory -> {
            TomcatEmbeddedServletContainerFactory tomcatFactory = (TomcatEmbeddedServletContainerFactory) factory;
            tomcatFactory.addConnectorCustomizers((TomcatConnectorCustomizer) connector -> {
                AbstractHttp11Protocol httpProtocol = (AbstractHttp11Protocol) connector.getProtocolHandler();
                httpProtocol.setCompression("on");
                httpProtocol.setCompressionMinSize(1);
                String mimeTypes = httpProtocol.getCompressableMimeTypes();
                String mimeTypesWithJson = mimeTypes + "," + MediaType.APPLICATION_JSON_VALUE;
                httpProtocol.setCompressableMimeTypes(mimeTypesWithJson);
            });
        };
    }

    @Bean
    public FilterRegistrationBean requestLoggingFilter() {
        FilterRegistrationBean registration = new FilterRegistrationBean();
        CommonsRequestLoggingFilter loggingFilter = new CommonsRequestLoggingFilter();
        loggingFilter.setIncludeClientInfo(true);
        loggingFilter.setIncludeQueryString(true);
        loggingFilter.setIncludePayload(true);
        registration.setFilter(loggingFilter);
        registration.addUrlPatterns("/*");
        registration.setOrder(Integer.MAX_VALUE);
        return registration;
    }

//    @Bean
    public FilterRegistrationBean requestDumperFilter() {
        FilterRegistrationBean registration = new FilterRegistrationBean();
        Filter requestDumperFilter = new RequestDumperFilter();
        registration.setFilter(requestDumperFilter);
        registration.addUrlPatterns("/*");
        return registration;
    }
}
