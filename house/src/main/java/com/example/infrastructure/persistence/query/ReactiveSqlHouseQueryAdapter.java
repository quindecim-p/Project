package com.example.infrastructure.persistence.query;

import com.example.application.ports.HouseQueryService;
import com.example.application.ports.ReactiveHouseQueryPort;
import com.example.infrastructure.web.dto.response.HouseResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ReactiveSqlHouseQueryAdapter implements ReactiveHouseQueryPort {

    private final HouseQueryService blockingQueryService;

    @Override
    public Mono<HouseResponse> findById(UUID id) {
        return Mono.fromCallable(() -> blockingQueryService.findById(id).orElse(null))
                .subscribeOn(Schedulers.boundedElastic());
    }

    @Override
    public Flux<HouseResponse> findAll() {
        return Mono.fromCallable(blockingQueryService::findAll)
                .subscribeOn(Schedulers.boundedElastic())
                .flatMapIterable(list -> list);
    }
}