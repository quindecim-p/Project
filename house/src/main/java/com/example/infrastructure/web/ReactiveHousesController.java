package com.example.infrastructure.web;

import com.example.application.ports.ReactiveHouseQueryPort;
import com.example.infrastructure.web.dto.response.HouseResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@RestController
@RequestMapping("/houses/webflux")
@RequiredArgsConstructor
public class ReactiveHousesController {

    private final ReactiveHouseQueryPort reactiveQueryPort;

    @GetMapping("/{id}")
    public Mono<ResponseEntity<HouseResponse>> getHouse(@PathVariable UUID id) {
        return reactiveQueryPort.findById(id)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @GetMapping
    public Flux<HouseResponse> getAllHouses() {
        return reactiveQueryPort.findAll();
    }
}