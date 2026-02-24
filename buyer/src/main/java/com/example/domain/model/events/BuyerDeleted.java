package com.example.domain.model.events;

import java.util.UUID;

public record BuyerDeleted(UUID buyerId) implements DomainEvent {}