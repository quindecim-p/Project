package com.example.infrastructure.web;

import com.example.application.ports.ReactiveHouseQueryPort;
import com.example.infrastructure.web.dto.response.HouseResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@RestController
@RequestMapping("/webflux/houses")
@RequiredArgsConstructor
public class ReactiveHousesController {

    private final ReactiveHouseQueryPort reactiveQueryPort;

    private static final Logger log = LoggerFactory.getLogger(ReactiveHousesController.class);

    @GetMapping("/{id}")
    public Mono<ResponseEntity<HouseResponse>> getHouse(@PathVariable UUID id) {
        log.info(">>> Запрос на получение дома {} ПРИНЯТ потоком: {}", id, Thread.currentThread().getName());

        return reactiveQueryPort.findById(id)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build())
                .doOnSuccess(response -> log.info("<<< Ответ по дому {} СФОРМИРОВАН потоком: {}", id, Thread.currentThread().getName()));
    }

    @GetMapping("/fast")
    public Mono<String> getFastResponse() {
        log.info(">>> Запрос на /fast ПРИНЯТ потоком: {}", Thread.currentThread().getName());
        return Mono.just("Быстрый ответ");
    }

    @GetMapping
    public Flux<HouseResponse> getAllHouses() {
        log.info(">>> Запрос на получение домов ПРИНЯТ потоком: {}", Thread.currentThread().getName());

        return reactiveQueryPort.findAll()
                .doOnNext(response -> log.info("<<< Возврат дома в потоке: {}", Thread.currentThread().getName()));
    }
}