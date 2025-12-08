package java.ru.practicum.statsclient;

import org.springframework.http.*;
import org.springframework.lang.Nullable;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;
import ru.practicum.statsdto.EndpointHit;
import ru.practicum.statsdto.ViewStats;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class StatsClient {
    private final RestTemplate rest;
    private final String serverUrl;

    public StatsClient(String serverUrl) {
        this.rest = new RestTemplate();
        this.serverUrl = serverUrl;
    }

    public void hit(EndpointHit endpointHit) {
        makeAndSendRequest("/hit", HttpMethod.POST, null, endpointHit);
    }

    public List<ViewStats> getStats(LocalDateTime start, LocalDateTime end,
                                    @Nullable List<String> uris, boolean unique) {

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

        ResponseEntity<ViewStats[]> response = makeAndSendRequest(
                path, HttpMethod.GET, parameters, null, ViewStats[].class);

        return response != null ? Arrays.asList(Objects.requireNonNull(response.getBody())) : Collections.emptyList();
    }

    private String encodeDateTime(LocalDateTime dateTime) {
        return URLEncoder.encode(
                dateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
                StandardCharsets.UTF_8
        );
    }

    private <T> ResponseEntity<T> makeAndSendRequest(String path, HttpMethod method,
                                                     @Nullable Map<String, Object> parameters,
                                                     @Nullable Object body,
                                                     Class<T> responseType) {
        HttpEntity<Object> requestEntity = new HttpEntity<>(body, defaultHeaders());

        ResponseEntity<T> response;
        try {
            if (parameters != null) {
                response = rest.exchange(serverUrl + path, method, requestEntity, responseType, parameters);
            } else {
                response = rest.exchange(serverUrl + path, method, requestEntity, responseType);
            }
        } catch (HttpStatusCodeException e) {
            throw new RuntimeException("Ошибка при выполнении запроса: " + e.getMessage(), e);
        }

        return response;
    }

    private void makeAndSendRequest(String path, HttpMethod method,
                                    @Nullable Map<String, Object> parameters,
                                    @Nullable Object body) {
        makeAndSendRequest(path, method, parameters, body, Object.class);
    }

    private HttpHeaders defaultHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        return headers;
    }
}