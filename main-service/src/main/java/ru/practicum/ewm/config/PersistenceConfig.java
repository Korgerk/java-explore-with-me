package ru.practicum.ewm.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableJpaRepositories(basePackages = "ru.practicum.ewm.repository")
@EnableJpaAuditing
@EnableTransactionManagement
public class PersistenceConfig {
}