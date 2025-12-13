package ru.practicum.ewm;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;
import ru.practicum.ewm.stats.client.StatsClient;

@Configuration
public class ExploreWithMeConfig {

    @Bean
    public StatsClient statsClient(@Value("${stats-server.url}") String url) {
        return new StatsClient(RestClient.builder().baseUrl(url).build());
    }
}
