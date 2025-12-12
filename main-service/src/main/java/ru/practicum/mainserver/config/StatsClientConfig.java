package ru.practicum.mainserver.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import java.ru.practicum.statsclient.StatsClient;

@Configuration
public class StatsClientConfig {

    @Value("${stats-service.url}")
    private String statsServiceUrl;

    @Bean
    public RestTemplate statsRestTemplate() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(2000);
        factory.setReadTimeout(2000);
        return new RestTemplate(factory);
    }

    @Bean
    public StatsClient statsClient(RestTemplate statsRestTemplate) {
        return new StatsClient(statsRestTemplate, statsServiceUrl);
    }
}
