package com.example.application.ports;

import java.util.UUID;

public interface ExternalValidationPort {
    void checkHouseEncumbrances(UUID houseId);
    void checkBuyerSolvency(UUID buyerId);
}