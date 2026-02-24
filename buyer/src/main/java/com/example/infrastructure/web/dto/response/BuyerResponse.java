package com.example.infrastructure.web.dto.response;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.UUID;

public record BuyerResponse(UUID id, String name, BigDecimal balance) implements Serializable {}
