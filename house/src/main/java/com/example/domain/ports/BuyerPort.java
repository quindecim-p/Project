package com.example.domain.ports;

import java.math.BigDecimal;
import java.util.UUID;

public interface BuyerPort {
    void requestPayment(UUID buyerId, BigDecimal amount, UUID transactionId);
    void compensatePayment(UUID buyerId, BigDecimal amount, UUID transactionId);
}
