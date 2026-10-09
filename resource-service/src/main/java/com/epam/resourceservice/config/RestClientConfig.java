package com.epam.resourceservice.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class RestClientConfig {

    @Bean
    public RestClient.Builder restClientBuilder() {
        return RestClient.builder();
    }

    @Bean
    public RestClient songServiceRestClient(
            RestClient.Builder builder,
            @Value("${song-service.url}") String songServiceUrl
    ) {
        return builder
                .baseUrl(songServiceUrl)
                .build();
    }
}