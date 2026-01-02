package com.codeit.closet.module.ws.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class ApiClientConfig {
    @Bean
    public RestClient dmApiRestClient(
                                       RestClient.Builder builder,
                                       @Value("${dm.api.base-url}") String baseUrl
    ) {
        return builder.baseUrl(baseUrl).build();
    }
}
