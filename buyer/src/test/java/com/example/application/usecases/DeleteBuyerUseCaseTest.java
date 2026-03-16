package com.example.application.usecases;

import com.example.domain.model.events.BuyerCreated;
import com.example.domain.model.events.BuyerDeleted;
import com.example.domain.model.events.DomainEvent;
import com.example.domain.ports.EventStore;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DeleteBuyerUseCaseTest {

    @Mock
    private EventStore eventStore;

    @InjectMocks
    private DeleteBuyerUseCase deleteBuyerUseCase;

    @Test
    void shouldDeleteBuyerSuccessfully() {
        UUID buyerId = UUID.randomUUID();
        when(eventStore.load(buyerId)).thenReturn(
                List.of(new BuyerCreated(buyerId, "Name"))
        );

        deleteBuyerUseCase.execute(buyerId);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<DomainEvent>> eventCaptor = ArgumentCaptor.forClass(List.class);
        verify(eventStore).save(eq(buyerId), eventCaptor.capture());

        assertEquals(BuyerDeleted.class, eventCaptor.getValue().getFirst().getClass());
    }

    @Test
    void shouldThrowExceptionIfBuyerNotFound() {
        UUID buyerId = UUID.randomUUID();
        when(eventStore.load(buyerId)).thenReturn(Collections.emptyList());

        assertThrows(IllegalArgumentException.class, () -> deleteBuyerUseCase.execute(buyerId));
    }
}