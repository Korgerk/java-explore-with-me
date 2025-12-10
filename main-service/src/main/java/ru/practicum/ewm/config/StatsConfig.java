package ru.practicum.ewm.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class StatsConfig {

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @Bean
    public ru.practicum.statsclient.StatsClient statsClient(@Value("${stats-server.url}") String serverUrl) {
        return new ru.practicum.statsclient.StatsClient(serverUrl);
    }
}