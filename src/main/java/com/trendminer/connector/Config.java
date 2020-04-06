package com.trendminer.connector;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.client.support.BasicAuthenticationInterceptor;

@Configuration
public class Config {

    @Bean
    public RestTemplate restTemplate(@Value("${influx.username}") String username,
                                     @Value("${influx.password}") String password) {
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.getInterceptors().add(new BasicAuthenticationInterceptor(username, password));

        return restTemplate;
    }
}
