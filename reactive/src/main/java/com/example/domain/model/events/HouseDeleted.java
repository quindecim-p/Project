package com.example.domain.model.events;

import java.util.UUID;

public record HouseDeleted(UUID houseId) implements DomainEvent {}