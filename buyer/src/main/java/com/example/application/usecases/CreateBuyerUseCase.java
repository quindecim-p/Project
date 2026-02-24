package com.example.application.usecases;

import com.example.domain.model.Buyer;
import com.example.domain.ports.EventStore;
import jakarta.transaction.Transactional;

import java.util.UUID;

public class CreateBuyerUseCase {

    private final EventStore eventStore;

    public CreateBuyerUseCase(EventStore eventStore) {
        this.eventStore = eventStore;
    }

    @Transactional
    public UUID execute(String name) {
        Buyer buyer = new Buyer(name);
        eventStore.save(buyer.getId(), buyer.getUncommitedChanges());
        return buyer.getId();
    }

}
