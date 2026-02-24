package com.example.application.usecases;

import com.example.domain.model.House;
import com.example.domain.ports.EventStore;
import jakarta.transaction.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

public class CreateHouseUseCase {

    private final EventStore eventStore;

    public CreateHouseUseCase(EventStore eventStore) {
        this.eventStore = eventStore;
    }

    @Transactional
    public UUID execute(String address, BigDecimal price) {
        House house = new House(address, price);
        eventStore.save(house.getId(), house.getUncommitedChanges());
        return house.getId();
    }

}
