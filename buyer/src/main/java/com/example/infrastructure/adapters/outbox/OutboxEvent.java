package com.example.infrastructure.adapters.outbox;

import jakarta.persistence.*;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "buyer_outbox")
@Data
public class OutboxEvent {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    private String topic;
    private String messageKey;
    @Column(columnDefinition = "TEXT")
    private String payload;
    private Instant createdAt = Instant.now();
}