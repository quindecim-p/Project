package com.example.application.sagas;

import com.example.application.usecases.BuyHouseUseCase;
import com.example.domain.model.House;
import com.example.domain.model.exceptions.BusinessException;
import com.example.domain.model.exceptions.EntityNotFoundException;
import com.example.domain.ports.BuyerPort;
import com.example.domain.ports.EventStore;
import lombok.extern.slf4j.Slf4j;

import java.util.UUID;

@Slf4j
public class AsyncBuyHouseSaga {

    private final EventStore eventStore;
    private final BuyerPort buyerPort;
    private final BuyHouseUseCase buyHouseUseCase;

    public AsyncBuyHouseSaga(EventStore eventStore, BuyerPort buyerPort, BuyHouseUseCase buyHouseUseCase) {
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

        if (house.isSold()) {
            throw new BusinessException("Этот дом уже продан!");
        }

        UUID transactionId = UUID.randomUUID();

        buyerPort.requestPayment(houseId, buyerId, house.getPrice(), transactionId);
    }

    public void onPaymentResult(UUID transactionId, UUID houseId, UUID buyerId, boolean isSuccess) {
        if (!isSuccess) {
            log.error("Списание не удалось. Сделка отменена. TxId: {}", transactionId);
            return;
        }

        try {
            buyHouseUseCase.execute(houseId, buyerId);
            log.info("Дом передан покупателю. TxId: {}", transactionId);
        } catch (Exception e) {
            log.error("Ошибка сохранения в БД. TxId: {}", transactionId, e);
            var house = new House(houseId, eventStore.load(houseId));
            buyerPort.compensatePayment(houseId, buyerId, house.getPrice(), transactionId);
        }
    }
}