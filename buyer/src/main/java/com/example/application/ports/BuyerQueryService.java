package com.example.application.ports;

import com.example.infrastructure.web.dto.response.BuyerResponse;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BuyerQueryService {
    Optional<BuyerResponse> findById(UUID id);
    List<BuyerResponse> findAll();
}
