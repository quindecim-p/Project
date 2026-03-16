package com.example.application.usecases;

import com.example.domain.model.events.BuyerCreated;
import com.example.domain.model.events.DomainEvent;
import com.example.domain.ports.EventStore;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class CreateBuyerUseCaseTest {

    @Mock private EventStore eventStore;
    @InjectMocks private CreateBuyerUseCase createBuyerUseCase;

    @Test
    void shouldCreateBuyerAndSaveEvent() {
        UUID buyerId = createBuyerUseCase.execute("Name");

        assertNotNull(buyerId);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<DomainEvent>> eventCaptor = ArgumentCaptor.forClass(List.class);
        verify(eventStore).save(eq(buyerId), eventCaptor.capture());

        List<DomainEvent> events = eventCaptor.getValue();
        assertEquals(1, events.size());
        BuyerCreated event = (BuyerCreated) events.getFirst();
        assertEquals("Name", event.name());
    }
}