package com.example.application.usecases;

import com.example.domain.model.events.BuyerCreated;
import com.example.domain.model.events.DomainEvent;
import com.example.domain.model.events.MoneyDeposited;
import com.example.domain.ports.EventStore;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DepositMoneyUseCaseTest {

    @Mock private EventStore eventStore;
    @InjectMocks private DepositMoneyUseCase depositMoneyUseCase;

    @Test
    void shouldDepositMoneySuccessfully() {
        UUID buyerId = UUID.randomUUID();
        when(eventStore.load(buyerId)).thenReturn(List.of(new BuyerCreated(buyerId, "Name")));

        depositMoneyUseCase.execute(buyerId, new BigDecimal("100"));

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<DomainEvent>> eventCaptor = ArgumentCaptor.forClass(List.class);
        verify(eventStore).save(eq(buyerId), eventCaptor.capture());

        MoneyDeposited event = (MoneyDeposited) eventCaptor.getValue().getFirst();
        assertEquals(new BigDecimal("100"), event.amount());
    }

    @Test
    void shouldThrowExceptionIfBuyerNotFound() {
        UUID buyerId = UUID.randomUUID();
        when(eventStore.load(buyerId)).thenReturn(Collections.emptyList());

        assertThrows(IllegalArgumentException.class, () -> depositMoneyUseCase.execute(buyerId, new BigDecimal("100")));
    }
}