package com.example.infrastructure.persistence;

import com.example.domain.model.events.*;
import com.example.domain.ports.EventStore;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.List;
import java.util.UUID;

@Repository
@Profile("!file")
@RequiredArgsConstructor
public class PostgresEventStore implements EventStore {

    private final JdbcTemplate jdbc;
    private final KafkaTemplate<String, String> kafka;
    private final ObjectMapper mapper;

    @Override
    @SneakyThrows
    public void save(UUID aggregateId, List<DomainEvent> events) {
        for (DomainEvent event : events) {
            String type = event.getClass().getSimpleName();
            String json = mapper.writeValueAsString(event);

            jdbc.update("INSERT INTO events (aggregate_id, event_type, payload) VALUES (?, ?, ?)",  aggregateId, type, json);

            String message = mapper.writeValueAsString(new KafkaWrapper(type, json));

            if (TransactionSynchronizationManager.isActualTransactionActive()) {
                TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                    @Override
                    public void afterCommit() {
                        kafka.send("domain-events", aggregateId.toString(), message);
                    }
                });
            } else {
                kafka.send("domain-events", aggregateId.toString(), message);
            }

        }
    }

    @Override
    @SneakyThrows
    public List<DomainEvent> load(UUID aggregateId) {
        return jdbc.query("SELECT event_type, payload FROM events WHERE aggregate_id = ? ORDER BY global_id",
                (rs, rowNum) -> {
                    String type = rs.getString("event_type");
                    String payload = rs.getString("payload");
                    Class<? extends DomainEvent> clazz = switch (type) {
                        case "BuyerCreated" -> BuyerCreated.class;
                        case "MoneyDeposited" -> MoneyDeposited.class;
                        case "MoneyWithdrawn" -> MoneyWithdrawn.class;
                        case "BuyerNameChanged" -> BuyerNameChanged.class;
                        case "BuyerDeleted" -> BuyerDeleted.class;
                        default -> throw new IllegalArgumentException("Unknown event type: " + type);
                    };
                    try {
                        return mapper.readValue(payload, clazz);
                    } catch (JsonProcessingException e) {
                        throw new RuntimeException(e);
                    }
                }, aggregateId);
    }

    public record KafkaWrapper(String type, String payload) {}
}
