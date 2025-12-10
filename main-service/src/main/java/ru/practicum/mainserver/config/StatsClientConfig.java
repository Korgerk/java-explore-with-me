package ru.practicum.mainserver.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.practicum.mainserver.client.StatsClient;


@Configuration
public class StatsClientConfig {

    @Value("${stats-service.url}")
    private String statsServiceUrl;

    @Bean
    public StatsClient statsClient() {
        return new StatsClient(statsServiceUrl);
    }
}