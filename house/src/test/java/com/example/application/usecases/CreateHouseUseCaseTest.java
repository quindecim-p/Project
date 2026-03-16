package com.example.application.usecases;

import com.example.domain.model.events.DomainEvent;
import com.example.domain.model.events.HouseCreated;
import com.example.domain.ports.EventStore;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class CreateHouseUseCaseTest {

    @Mock
    private EventStore eventStore;

    @InjectMocks
    private CreateHouseUseCase createHouseUseCase;

    @Test
    void shouldCreateHouseAndSaveEvent() {
        String address = "Address";
        BigDecimal price = new BigDecimal("50000");

        UUID houseId = createHouseUseCase.execute(address, price);

        assertNotNull(houseId);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<DomainEvent>> eventCaptor = ArgumentCaptor.forClass(List.class);

        verify(eventStore).save(eq(houseId), eventCaptor.capture());

        List<DomainEvent> savedEvents = eventCaptor.getValue();
        assertEquals(1, savedEvents.size());

        HouseCreated event = (HouseCreated) savedEvents.getFirst();
        assertEquals(address, event.address());
        assertEquals(price, event.price());
    }
}