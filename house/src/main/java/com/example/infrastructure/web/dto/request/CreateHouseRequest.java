package com.example.infrastructure.web.dto.request;

import java.math.BigDecimal;

public record CreateHouseRequest(String address, BigDecimal price) {}