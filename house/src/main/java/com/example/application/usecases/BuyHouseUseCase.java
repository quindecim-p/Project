package com.example.application.usecases;

import com.example.domain.model.House;
import com.example.domain.model.exceptions.EntityNotFoundException;
import com.example.domain.ports.BuyerPort;
import com.example.domain.ports.EventStore;
import jakarta.transaction.Transactional;

import java.util.UUID;

public class BuyHouseUseCase {

    private final EventStore eventStore;

    public BuyHouseUseCase(EventStore eventStore) {
        this.eventStore = eventStore;

    }

    @Transactional
    public void execute(UUID id, UUID buyerId) {
        var history = eventStore.load(id);

        if (history.isEmpty()) {
            throw new EntityNotFoundException("Дом с ID " + id + " не найден");
        }

        var house = new House(id, history);

        house.sellTo(buyerId);

        eventStore.save(id, house.getUncommitedChanges());
    }

}
