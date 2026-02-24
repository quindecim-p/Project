package com.example.domain.model.events;

import java.util.UUID;

public record HouseSold(UUID houseId, UUID newOwnerId) implements DomainEvent {}
