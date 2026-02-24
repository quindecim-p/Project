package com.example.infrastructure.web.dto.request;

import java.math.BigDecimal;

public record UpdateHouseRequest(String address, BigDecimal price) {}