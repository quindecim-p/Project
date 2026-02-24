package com.example.domain.ports;

import com.example.domain.model.events.DomainEvent;

import java.util.List;
import java.util.UUID;

public interface EventStore {
    void save(UUID aggregateId, List<DomainEvent> events);
    List<DomainEvent> load(UUID aggregateId);
}
