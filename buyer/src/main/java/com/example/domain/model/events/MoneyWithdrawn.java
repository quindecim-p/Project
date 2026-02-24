package com.example.domain.model.events;

import java.math.BigDecimal;
import java.util.UUID;

public record MoneyWithdrawn(UUID buyerId, BigDecimal amount) implements DomainEvent { }
