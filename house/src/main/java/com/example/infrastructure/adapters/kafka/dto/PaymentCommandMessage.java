package com.example.infrastructure.adapters.kafka.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record PaymentCommandMessage(
        UUID transactionId,
        UUID houseId,
        UUID buyerId,
        BigDecimal amount,
        String action
) {}