package com.example.application.usecases;

import com.example.domain.model.events.DomainEvent;
import com.example.domain.model.events.HouseCreated;
import com.example.domain.model.events.HouseUpdated;
import com.example.domain.model.exceptions.EntityNotFoundException;
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
class UpdateHouseUseCaseTest {

    @Mock
    private EventStore eventStore;

    @InjectMocks
    private UpdateHouseUseCase updateHouseUseCase;

    @Test
    void shouldUpdateHouseSuccessfully() {
        UUID houseId = UUID.randomUUID();
        when(eventStore.load(houseId)).thenReturn(
                List.of(new HouseCreated(houseId, "Old Address", new BigDecimal("100")))
        );

        updateHouseUseCase.execute(houseId, "New Address", new BigDecimal("200"));

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<DomainEvent>> eventCaptor = ArgumentCaptor.forClass(List.class);
        verify(eventStore).save(eq(houseId), eventCaptor.capture());

        HouseUpdated updatedEvent = (HouseUpdated) eventCaptor.getValue().getFirst();
        assertEquals("New Address", updatedEvent.address());
        assertEquals(new BigDecimal("200"), updatedEvent.price());
    }

    @Test
    void shouldThrowExceptionIfHouseNotFound() {
        UUID houseId = UUID.randomUUID();
        when(eventStore.load(houseId)).thenReturn(Collections.emptyList());

        assertThrows(EntityNotFoundException.class,
                () -> updateHouseUseCase.execute(houseId, "New Address", new BigDecimal("1")));
    }
}