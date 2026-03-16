package com.example.application.sagas;

import com.example.application.usecases.BuyHouseUseCase;
import com.example.domain.model.events.HouseCreated;
import com.example.domain.ports.BuyerPort;
import com.example.domain.ports.EventStore;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AsyncBuyHouseSagaTest {

    @Mock private EventStore eventStore;
    @Mock private BuyerPort buyerPort;
    @Mock private BuyHouseUseCase buyHouseUseCase;

    @InjectMocks private AsyncBuyHouseSaga saga;

    @Test
    void shouldRequestPaymentOnExecute() {
        UUID houseId = UUID.randomUUID();
        UUID buyerId = UUID.randomUUID();
        BigDecimal price = new BigDecimal("1000");

        when(eventStore.load(houseId)).thenReturn(
                List.of(new HouseCreated(houseId, "Address", price))
        );

        saga.execute(houseId, buyerId);

        verify(buyerPort, times(1))
                .requestPayment(eq(houseId), eq(buyerId), eq(price), any(UUID.class));
    }

    @Test
    void shouldExecuteBuyHouseWhenPaymentIsSuccess() {
        UUID transactionId = UUID.randomUUID();
        UUID houseId = UUID.randomUUID();
        UUID buyerId = UUID.randomUUID();

        saga.onPaymentResult(transactionId, houseId, buyerId, true);

        verify(buyHouseUseCase, times(1)).execute(houseId, buyerId);
        verify(buyerPort, never()).compensatePayment(any(), any(), any(), any());
    }

    @Test
    void shouldDoNothingWhenPaymentFailed() {
        saga.onPaymentResult(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), false);

        verify(buyHouseUseCase, never()).execute(any(), any());
    }

    @Test
    void shouldTriggerCompensationIfBuyHouseFails() {
        UUID transactionId = UUID.randomUUID();
        UUID houseId = UUID.randomUUID();
        UUID buyerId = UUID.randomUUID();
        BigDecimal price = new BigDecimal("1000");

        doThrow(new RuntimeException("DB Connection down"))
                .when(buyHouseUseCase).execute(houseId, buyerId);

        when(eventStore.load(houseId)).thenReturn(
                List.of(new HouseCreated(houseId, "Address", price))
        );

        saga.onPaymentResult(transactionId, houseId, buyerId, true);

        verify(buyerPort, times(1)).compensatePayment(houseId, buyerId, price, transactionId);
    }
}