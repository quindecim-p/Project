package com.example.application.usecases;

import com.example.domain.model.Buyer;
import com.example.domain.ports.EventStore;
import jakarta.transaction.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

public class WithdrawMoneyUseCase {

    private final EventStore eventStore;

    public WithdrawMoneyUseCase(EventStore eventStore) {
        this.eventStore = eventStore;
    }

    @Transactional
    public void execute(UUID buyerId, BigDecimal amount) {
        var history = eventStore.load(buyerId);

        if (history.isEmpty()) {
            throw new IllegalArgumentException("Покупатель с ID " + buyerId + " не найден");
        }

        var buyer = new Buyer(buyerId, history);

        buyer.withdraw(amount);

        eventStore.save(buyerId, buyer.getUncommitedChanges());
    }

}
