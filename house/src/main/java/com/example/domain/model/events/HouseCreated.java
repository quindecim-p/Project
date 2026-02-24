package com.example.domain.model.events;

import java.math.BigDecimal;
import java.util.UUID;

public record HouseCreated(UUID houseId, String address, BigDecimal price) implements DomainEvent {}
