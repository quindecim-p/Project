package com.example.application.usecases;

import com.example.domain.model.House;
import com.example.domain.model.exceptions.EntityNotFoundException;
import com.example.domain.ports.EventStore;
import jakarta.transaction.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

public class UpdateHouseUseCase {

    private final EventStore eventStore;

    public UpdateHouseUseCase(EventStore eventStore) {
        this.eventStore = eventStore;
    }

    @Transactional
    public void execute(UUID id, String address, BigDecimal price) {
        var history = eventStore.load(id);

        if (history.isEmpty()) {
            throw new EntityNotFoundException("Дом с ID " + id + " не найден");
        }

        var house = new House(id, history);

        house.update(address, price);

        eventStore.save(house.getId(), house.getUncommitedChanges());
    }
}