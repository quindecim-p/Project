package com.example.infrastructure.web.dto.response;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.UUID;

public record HouseResponse(UUID id, String address, BigDecimal price, String status, UUID ownerId) implements Serializable {}
