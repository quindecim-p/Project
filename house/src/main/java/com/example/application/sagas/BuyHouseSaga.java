package com.example.application.sagas;

import com.example.application.ports.ExternalValidationPort;
import com.example.application.usecases.BuyHouseUseCase;
import com.example.domain.model.House;
import com.example.domain.model.exceptions.BusinessException;
import com.example.domain.model.exceptions.CriticalConsistencyException;
import com.example.domain.model.exceptions.EntityNotFoundException;
import com.example.domain.ports.BuyerPort;
import com.example.domain.ports.EventStore;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;

public class BuyHouseSaga {

    private final EventStore eventStore;
    private final BuyerPort buyerPort;
    private final BuyHouseUseCase buyHouseUseCase;
    private final ExternalValidationPort validationPort;

    private static final ExecutorService VIRTUAL_EXECUTOR = Executors.newVirtualThreadPerTaskExecutor();
    private final Retry compensationRetry;

    public BuyHouseSaga(EventStore eventStore,
                        BuyerPort buyerPort,
                        BuyHouseUseCase buyHouseUseCase,
                        ExternalValidationPort validationPort) {
        this.eventStore = eventStore;
        this.buyerPort = buyerPort;
        this.buyHouseUseCase = buyHouseUseCase;
        this.validationPort = validationPort;

        this.compensationRetry = Retry.of("compensation", RetryConfig.custom()
                .maxAttempts(10)
                .waitDuration(Duration.ofSeconds(2))
                .build());
    }

    public void execute(UUID houseId, UUID buyerId) {
        var history = eventStore.load(houseId);

        if (history.isEmpty()) {
            throw new EntityNotFoundException("Дом с ID " + houseId + " не найден");
        }

        var house = new House(houseId, history);
        BigDecimal price = house.getPrice();

        UUID transactionId = UUID.randomUUID();

        runParallelValidations(houseId, buyerId);

        try {
            buyerPort.requestPayment(buyerId, price, transactionId);
        } catch (Exception e) {
            System.err.println("Сбой при списании. Запускаем отмену...");
            executeCompensation(buyerId, price, transactionId);
            throw new BusinessException("Платеж отклонен или недоступен: " + e.getMessage());
        }

        try {
            buyHouseUseCase.execute(houseId, buyerId);
        } catch (Exception e) {
            System.err.println("Ошибка при сохранении дома. Запускаем компенсацию " + price + "...");
            executeCompensation(buyerId, price, transactionId);
            throw e;
        }
    }

    private void runParallelValidations(UUID houseId, UUID buyerId) {
        AtomicReference<Thread> houseThread = new AtomicReference<>();
        AtomicReference<Thread> buyerThread = new AtomicReference<>();

        CompletableFuture<Void> houseValidationFuture = CompletableFuture.runAsync(
                () -> {
                    houseThread.set(Thread.currentThread());
                    validationPort.checkHouseEncumbrances(houseId);
                },
                VIRTUAL_EXECUTOR
        );

        CompletableFuture<Void> buyerValidationFuture = CompletableFuture.runAsync(
                () -> {
                    buyerThread.set(Thread.currentThread());
                    validationPort.checkBuyerSolvency(buyerId);
                },
                VIRTUAL_EXECUTOR
        );

        houseValidationFuture.exceptionally(ex -> {
            buyerValidationFuture.cancel(true);
            Thread targetThread = buyerThread.get();
            if (targetThread != null) {
                targetThread.interrupt();
            }
            return null;
        });

        buyerValidationFuture.exceptionally(ex -> {
            System.err.println("Ошибка валидации покупателя: " + ex.getMessage());
            houseValidationFuture.cancel(true);
            Thread targetThread = houseThread.get();
            if (targetThread != null) {
                targetThread.interrupt();
            }
            return null;
        });

        try {
            CompletableFuture.allOf(houseValidationFuture, buyerValidationFuture)
                    .get(10, TimeUnit.SECONDS);
        } catch (TimeoutException e) {
            houseValidationFuture.cancel(true);
            buyerValidationFuture.cancel(true);

            if (houseThread.get() != null) houseThread.get().interrupt();
            if (buyerThread.get() != null) buyerThread.get().interrupt();

            throw new BusinessException("Превышено время ожидания проверки");
        } catch (ExecutionException e) {
            throw new BusinessException("Сделка отклонена");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Поток выполнения был прерван", e);
        }
    }

    private void executeCompensation(UUID buyerId, BigDecimal price, UUID transactionId) {
        try {
            Runnable compensateTask = Retry.decorateRunnable(compensationRetry, () ->
                    buyerPort.compensatePayment(buyerId, price, transactionId)
            );
            compensateTask.run();
        } catch (Exception e) {
            String alert = String.format("КРИТИЧЕСКАЯ ОШИБКА: Потеря денег! Клиент: %s, Сумма: %s, TxID: %s",
                    buyerId, price, transactionId);
            System.err.println(alert);
            throw new CriticalConsistencyException(alert);
        }
    }
}