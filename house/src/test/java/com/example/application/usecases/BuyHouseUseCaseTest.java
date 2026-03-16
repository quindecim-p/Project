package com.example.application.usecases;

import com.example.domain.model.events.HouseCreated;
import com.example.domain.model.exceptions.EntityNotFoundException;
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
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BuyHouseUseCaseTest {

    @Mock
    private EventStore eventStore;

    @InjectMocks
    private BuyHouseUseCase buyHouseUseCase;

    @Test
    void shouldExecuteBuyHouseSuccessfully() {
        UUID houseId = UUID.randomUUID();
        UUID buyerId = UUID.randomUUID();

        when(eventStore.load(houseId)).thenReturn(
                List.of(new HouseCreated(houseId, "Address", new BigDecimal("1000")))
        );

        buyHouseUseCase.execute(houseId, buyerId);

        verify(eventStore, times(1)).save(eq(houseId), anyList());
    }

    @Test
    void shouldThrowExceptionIfHouseNotFound() {
        UUID houseId = UUID.randomUUID();
        UUID buyerId = UUID.randomUUID();

        when(eventStore.load(houseId)).thenReturn(Collections.emptyList());

        assertThrows(EntityNotFoundException.class, () -> buyHouseUseCase.execute(houseId, buyerId));

        verify(eventStore, never()).save(any(), any());
    }
}