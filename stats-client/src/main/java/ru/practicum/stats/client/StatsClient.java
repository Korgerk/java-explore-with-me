package ru.practicum.stats.client;

import org.springframework.web.client.RestTemplate;
import ru.practicum.stats.Hit;
import ru.practicum.stats.ViewStats;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class StatsClient {

    private final RestTemplate restTemplate;
    private final String statsServerUrl;

    public StatsClient(RestTemplate restTemplate, String statsServerUrl) {
        this.restTemplate = restTemplate;
        this.statsServerUrl = statsServerUrl;
    }

    public void addHit(Hit hit) {
        restTemplate.postForEntity(statsServerUrl + "/hit", hit, Void.class);
    }

    public List<ViewStats> getStats(LocalDateTime start, LocalDateTime end, List<String> uris, boolean unique) {
        String uri = String.format("%s/stats?start=%s&end=%s&unique=%s", statsServerUrl, start.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")), end.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")), unique);

        if (uris != null && !uris.isEmpty()) {
            String urisParam = uris.stream().map(u -> "uris=" + u).collect(Collectors.joining("&"));
            uri += "&" + urisParam;
        }

        ViewStats[] response = restTemplate.getForObject(uri, ViewStats[].class);
        return response != null ? Arrays.asList(response) : List.of();
    }
}