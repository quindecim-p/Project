package com.example.infrastructure.adapters.kafka.dto;

import java.util.UUID;

public record PaymentResponseMessage(
        UUID transactionId,
        UUID houseId,
        UUID buyerId,
        boolean success,
        String errorMessage
) {}
