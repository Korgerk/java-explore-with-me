package ru.practicum.stats.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.stats.model.EndpointHit;

import java.time.LocalDateTime;
import java.util.List;

public interface StatsRepository extends JpaRepository<EndpointHit, Long> {

    @Query("SELECT e FROM EndpointHit e " +
           "WHERE e.timestamp BETWEEN :start AND :end " +
           "AND (:uris IS NULL OR e.uri IN :uris)")
    List<EndpointHit> findHits(@Param("start") LocalDateTime start,
                               @Param("end") LocalDateTime end,
                               @Param("uris") List<String> uris);

    @Query("SELECT e FROM EndpointHit e " +
           "WHERE e.timestamp BETWEEN :start AND :end " +
           "AND e.ip = :ip " +
           "AND e.uri = :uri")
    List<EndpointHit> findUniqueHits(@Param("start") LocalDateTime start,
                                     @Param("end") LocalDateTime end,
                                     @Param("ip") String ip,
                                     @Param("uri") String uri);
}