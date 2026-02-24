package com.example.domain.model.events;

import java.util.UUID;

public record BuyerCreated(UUID buyerId, String name) implements DomainEvent {}
