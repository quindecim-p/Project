package com.example.application.sagas;

import com.example.application.ports.ExternalValidationPort;
import com.example.application.usecases.BuyHouseUseCase;
import com.example.domain.model.events.HouseCreated;
import com.example.domain.model.exceptions.BusinessException;
import com.example.domain.model.exceptions.EntityNotFoundException;
import com.example.domain.ports.BuyerPort;
import com.example.domain.ports.EventStore;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BuyHouseSagaTest {

    @Mock private EventStore eventStore;
    @Mock private BuyerPort buyerPort;
    @Mock private BuyHouseUseCase buyHouseUseCase;
    @Mock private ExternalValidationPort validationPort;

    @InjectMocks private BuyHouseSaga buyHouseSaga;

    @Test
    void shouldExecuteSagaSuccessfully() {
        UUID houseId = UUID.randomUUID();
        UUID buyerId = UUID.randomUUID();
        BigDecimal price = new BigDecimal("5000");

        when(eventStore.load(houseId)).thenReturn(
                List.of(new HouseCreated(houseId, "Address", price))
        );

        buyHouseSaga.execute(houseId, buyerId);

        verify(buyerPort).requestPayment(eq(houseId), eq(buyerId), eq(price), any(UUID.class));
        verify(buyHouseUseCase).execute(houseId, buyerId);
        verify(buyerPort, never()).compensatePayment(any(), any(), any(), any());
    }

    @Test
    void shouldThrowEntityNotFoundIfHistoryIsEmpty() {
        UUID houseId = UUID.randomUUID();
        UUID buyerId = UUID.randomUUID();

        when(eventStore.load(houseId)).thenReturn(Collections.emptyList());

        assertThrows(EntityNotFoundException.class, () -> buyHouseSaga.execute(houseId, buyerId));
    }

    @Test
    void shouldCompensateAndThrowIfPaymentFails() {
        UUID houseId = UUID.randomUUID();
        UUID buyerId = UUID.randomUUID();
        BigDecimal price = new BigDecimal("5000");

        when(eventStore.load(houseId)).thenReturn(
                List.of(new HouseCreated(houseId, "Address", price))
        );

        doThrow(new RuntimeException("Payment Service Down"))
                .when(buyerPort).requestPayment(eq(houseId), eq(buyerId), eq(price), any(UUID.class));

        assertThrows(BusinessException.class, () -> buyHouseSaga.execute(houseId, buyerId));

        verify(buyerPort, times(1)).compensatePayment(eq(houseId), eq(buyerId), eq(price), any(UUID.class));

        verify(buyHouseUseCase, never()).execute(any(), any());
    }

    @Test
    void shouldCompensateAndThrowIfUseCaseFails() {
        UUID houseId = UUID.randomUUID();
        UUID buyerId = UUID.randomUUID();
        BigDecimal price = new BigDecimal("5000");

        when(eventStore.load(houseId)).thenReturn(
                List.of(new HouseCreated(houseId, "Address", price))
        );

        doThrow(new RuntimeException("Database timeout"))
                .when(buyHouseUseCase).execute(houseId, buyerId);

        assertThrows(RuntimeException.class, () -> buyHouseSaga.execute(houseId, buyerId));

        verify(buyerPort, times(1)).compensatePayment(eq(houseId), eq(buyerId), eq(price), any(UUID.class));
    }
}