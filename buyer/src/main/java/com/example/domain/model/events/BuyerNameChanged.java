package com.example.domain.model.events;

import java.util.UUID;

public record BuyerNameChanged(UUID buyerId, String newName) implements DomainEvent {}