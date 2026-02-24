package com.example.domain.model.events;

import java.math.BigDecimal;
import java.util.UUID;

public record HouseUpdated(UUID houseId, String address, BigDecimal price) implements DomainEvent {}