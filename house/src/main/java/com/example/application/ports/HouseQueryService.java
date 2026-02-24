package com.example.application.ports;

import com.example.infrastructure.web.dto.response.HouseResponse;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface HouseQueryService {
    Optional<HouseResponse> findById(UUID id);
    List<HouseResponse> findAll();
}
