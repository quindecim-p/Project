package com.example.application.ports;

import com.example.infrastructure.web.dto.response.HouseResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface ReactiveHouseQueryPort {
    Mono<HouseResponse> findById(UUID id);
    Flux<HouseResponse> findAll();
}