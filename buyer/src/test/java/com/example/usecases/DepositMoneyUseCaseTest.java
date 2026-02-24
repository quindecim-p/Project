package com.example.usecases;

import com.example.application.usecases.DepositMoneyUseCase;
import com.example.domain.model.events.BuyerCreated;
import com.example.domain.ports.EventStore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.*;

class DepositMoneyUseCaseTest {

    private EventStore eventStore;
    private DepositMoneyUseCase useCase;

    @BeforeEach
    void setUp() {
        eventStore = Mockito.mock(EventStore.class);
        useCase = new DepositMoneyUseCase(eventStore);
    }

    @Test
    @DisplayName("Депозит должен выполняться при пополнении")
    void execute_ShouldCompleteDeposit_WhenAllConditionsAreOk() {
        UUID buyerId = UUID.randomUUID();

        when(eventStore.load(buyerId)).thenReturn(List.of(
                new BuyerCreated(buyerId, "Name")
        ));

        useCase.execute(buyerId, new BigDecimal("100"));

        verify(eventStore, times(1)).load(buyerId);
    }

}