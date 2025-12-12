package java.ru.practicum.statsclient;

import org.springframework.http.*;
import org.springframework.lang.Nullable;
import org.springframework.web.client.RestTemplate;
import ru.practicum.statsdto.EndpointHit;
import ru.practicum.statsdto.ViewStats;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StatsClient {

    private final RestTemplate rest;
    private final String serverUrl;

    public StatsClient(RestTemplate restTemplate, String serverUrl) {
        this.rest = restTemplate;
        this.serverUrl = serverUrl;
    }

    public void hit(EndpointHit endpointHit) {
        try {
            makeAndSendRequest("/hit", HttpMethod.POST, null, endpointHit, Void.class);
        } catch (Exception ignored) {
        }
    }

    public List<ViewStats> getStats(LocalDateTime start, LocalDateTime end, @Nullable List<String> uris, boolean unique) {
        try {
            Map<String, Object> parameters = new HashMap<>();
            parameters.put("start", encodeDateTime(start));
            parameters.put("end", encodeDateTime(end));

            String path = "/stats?start={start}&end={end}";

            if (uris != null && !uris.isEmpty()) {
                parameters.put("uris", String.join(",", uris));
                path += "&uris={uris}";
            }

            if (unique) {
                parameters.put("unique", true);
                path += "&unique={unique}";
            }

            ResponseEntity<ViewStats[]> response = makeAndSendRequest(path, HttpMethod.GET, parameters, null, ViewStats[].class);

            return response.getBody() == null ? List.of() : List.of(response.getBody());
        } catch (Exception e) {
            return List.of();
        }
    }

    private String encodeDateTime(LocalDateTime dateTime) {
        return URLEncoder.encode(dateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")), StandardCharsets.UTF_8);
    }

    private <T> ResponseEntity<T> makeAndSendRequest(String path, HttpMethod method, @Nullable Map<String, Object> parameters, @Nullable Object body, Class<T> responseType) {

        HttpEntity<Object> requestEntity = new HttpEntity<>(body, defaultHeaders());

        if (parameters != null) {
            return rest.exchange(serverUrl + path, method, requestEntity, responseType, parameters);
        } else {
            return rest.exchange(serverUrl + path, method, requestEntity, responseType);
        }
    }

    private HttpHeaders defaultHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        return headers;
    }
}
