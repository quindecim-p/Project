package com.example.application.usecases;

import com.example.domain.model.Buyer;
import com.example.domain.model.events.DomainEvent;
import com.example.domain.ports.EventStore;
import jakarta.transaction.Transactional;

import java.util.List;
import java.util.UUID;

public class DeleteBuyerUseCase {

    private final EventStore eventStore;

    public DeleteBuyerUseCase(EventStore eventStore) {
        this.eventStore = eventStore;
    }

    @Transactional
    public void execute(UUID id) {
        var history = eventStore.load(id);

        if (history.isEmpty()) {
            throw new IllegalArgumentException("Buyer not found");
        }

        var buyer = new Buyer(id, history);

        buyer.delete();

        eventStore.save(buyer.getId(), buyer.getUncommitedChanges());
    }
}