package com.epam.resourceservice.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.web.client.RestClient;

@Configuration
public class RestClientConfig {

    @Bean
    @Primary
    public RestClient.Builder restClientBuilder() {
        return RestClient.builder();
    }

    @Bean
    @LoadBalanced
    public RestClient.Builder loadBalancedRestClientBuilder() {
        return RestClient.builder();
    }

    @Bean
    public RestClient songServiceRestClient(
            @Qualifier("loadBalancedRestClientBuilder")
            RestClient.Builder builder,
            @Value("${song-service.url:http://song-service}")
            String songServiceUrl
    ) {
        return builder
                .baseUrl(songServiceUrl)
                .build();
    }
}
