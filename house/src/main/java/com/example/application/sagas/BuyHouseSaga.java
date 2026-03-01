package com.example.application.sagas;

import com.example.application.ports.ExternalValidationPort;
import com.example.application.usecases.BuyHouseUseCase;
import com.example.domain.model.House;
import com.example.domain.model.exceptions.BusinessException;
import com.example.domain.model.exceptions.EntityNotFoundException;
import com.example.domain.ports.BuyerPort;
import com.example.domain.ports.EventStore;

import java.math.BigDecimal;
import java.util.UUID;
import java.util.concurrent.*;

public class BuyHouseSaga {

    private final EventStore eventStore;
    private final BuyerPort buyerPort;
    private final BuyHouseUseCase buyHouseUseCase;
    private final ExternalValidationPort validationPort;

    private final ExecutorService virtualThreadExecutor = Executors.newVirtualThreadPerTaskExecutor();

    public BuyHouseSaga(EventStore eventStore,
                        BuyerPort buyerPort,
                        BuyHouseUseCase buyHouseUseCase,
                        ExternalValidationPort validationPort) {
        this.eventStore = eventStore;
        this.buyerPort = buyerPort;
        this.buyHouseUseCase = buyHouseUseCase;
        this.validationPort = validationPort;
    }

    public void execute(UUID houseId, UUID buyerId) {
        var history = eventStore.load(houseId);

        if (history.isEmpty()) {
            throw new EntityNotFoundException("Дом с ID " + houseId + " не найден");
        }

        var house = new House(houseId, history);
        BigDecimal price = house.getPrice();

        runParallelValidations(houseId, buyerId);

        buyerPort.requestPayment(buyerId, price);

        try {
            buyHouseUseCase.execute(houseId, buyerId);
        } catch (Exception e) {
            System.err.println("Ошибка при сохранении дома. Запускаем компенсацию (возврат " + price + ")...");
            buyerPort.compensatePayment(buyerId, price);
            throw e;
        }
    }

    private void runParallelValidations(UUID houseId, UUID buyerId) {
        CompletableFuture<Void> houseValidationFuture = CompletableFuture.runAsync(
                () -> validationPort.checkHouseEncumbrances(houseId),
                virtualThreadExecutor
        );

        CompletableFuture<Void> buyerValidationFuture = CompletableFuture.runAsync(
                () -> validationPort.checkBuyerSolvency(buyerId),
                virtualThreadExecutor
        );

        houseValidationFuture.exceptionally(ex -> {
            buyerValidationFuture.cancel(true);
            return null;
        });

        buyerValidationFuture.exceptionally(ex -> {
            houseValidationFuture.cancel(true);
            return null;
        });

        try {
            CompletableFuture.allOf(houseValidationFuture, buyerValidationFuture).join();
        } catch (CompletionException e) {
            throw new BusinessException("Сделка отклонена службой безопасности: " + e.getCause().getMessage());
        }
    }
}