package com.example.application.usecases;

import com.example.domain.model.House;
import com.example.domain.model.exceptions.EntityNotFoundException;
import com.example.domain.ports.EventStore;
import jakarta.transaction.Transactional;

import java.util.UUID;

public class DeleteHouseUseCase {

    private final EventStore eventStore;

    public DeleteHouseUseCase(EventStore eventStore) {
        this.eventStore = eventStore;
    }

    @Transactional
    public void execute(UUID id) {
        var history = eventStore.load(id);

        if (history.isEmpty()) {
            throw new EntityNotFoundException("Дом с ID " + id + " не найден");
        }

        var house = new House(id, history);

        house.delete();

        eventStore.save(house.getId(), house.getUncommitedChanges());
    }
}