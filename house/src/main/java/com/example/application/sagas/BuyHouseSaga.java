package com.example.application.sagas;

import com.example.application.usecases.BuyHouseUseCase;
import com.example.domain.model.House;
import com.example.domain.model.exceptions.EntityNotFoundException;
import com.example.domain.ports.BuyerPort;
import com.example.domain.ports.EventStore;

import java.math.BigDecimal;
import java.util.UUID;

public class BuyHouseSaga {

    private final EventStore eventStore;
    private final BuyerPort buyerPort;
    private final BuyHouseUseCase buyHouseUseCase;

    public BuyHouseSaga(EventStore eventStore,
                        BuyerPort buyerPort,
                        BuyHouseUseCase buyHouseUseCase) {
        this.eventStore = eventStore;
        this.buyerPort = buyerPort;
        this.buyHouseUseCase = buyHouseUseCase;
    }

    public void execute(UUID houseId, UUID buyerId) {
        var history = eventStore.load(houseId);

        if (history.isEmpty()) {
            throw new EntityNotFoundException("Дом с ID " + houseId + " не найден");
        }

        var house = new House(houseId, history);

        BigDecimal price = house.getPrice();

        buyerPort.requestPayment(buyerId, price);

        try {
            buyHouseUseCase.execute(houseId, buyerId);
        } catch (Exception e) {
            buyerPort.compensatePayment(buyerId, price);
            throw e;
        }
    }
}