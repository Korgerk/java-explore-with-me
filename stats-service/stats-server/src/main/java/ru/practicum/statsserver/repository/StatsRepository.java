package ru.practicum.statsserver.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.statsdto.ViewStats;
import ru.practicum.statsserver.model.Stats;

import java.time.LocalDateTime;
import java.util.List;

public interface StatsRepository extends JpaRepository<Stats, Long> {

    @Query("SELECT new ru.practicum.statsdto.ViewStats(s.app, s.uri, COUNT(s.ip)) " +
           "FROM Stats AS s " +
           "WHERE s.timestamp BETWEEN ?1 AND ?2 " +
           "AND (s.uri IN ?3 OR ?3 IS NULL) " +
           "GROUP BY s.app, s.uri " +
           "ORDER BY COUNT(s.ip) DESC")
    List<ViewStats> getStats(LocalDateTime start, LocalDateTime end, List<String> uris);

    @Query("SELECT new ru.practicum.statsdto.ViewStats(s.app, s.uri, COUNT(DISTINCT s.ip)) " +
           "FROM Stats AS s " +
           "WHERE s.timestamp BETWEEN ?1 AND ?2 " +
           "AND (s.uri IN ?3 OR ?3 IS NULL) " +
           "GROUP BY s.app, s.uri " +
           "ORDER BY COUNT(DISTINCT s.ip) DESC")
    List<ViewStats> getStatsUnique(LocalDateTime start, LocalDateTime end, List<String> uris);
}