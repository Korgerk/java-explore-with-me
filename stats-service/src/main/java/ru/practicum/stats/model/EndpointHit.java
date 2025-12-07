package ru.practicum.stats.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "endpoint_hits")
@Getter
@Setter
@RequiredArgsConstructor
@AllArgsConstructor
public class EndpointHit {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String app;
    private String uri;
    private String ip;
    private LocalDateTime timestamp;
}