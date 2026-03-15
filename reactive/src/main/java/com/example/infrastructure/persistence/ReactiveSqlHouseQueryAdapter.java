package com.example.infrastructure.persistence;

import com.example.application.ports.ReactiveHouseQueryPort;
import com.example.infrastructure.web.dto.response.HouseResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import io.r2dbc.spi.Row;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ReactiveSqlHouseQueryAdapter implements ReactiveHouseQueryPort {

    private final DatabaseClient databaseClient;

    private static final Logger log = LoggerFactory.getLogger(ReactiveSqlHouseQueryAdapter.class);

    @Override
    public Mono<HouseResponse> findById(UUID id) {
        String sql = "SELECT * FROM houses_view WHERE house_id = $1";

        log.info(">>> Начинаем обращение к БД в потоке: {}", Thread.currentThread().getName());

        return databaseClient.sql(sql)
                .bind(0, id)
                .map((row, metadata) -> mapRow(row))
                .one()
                .delayElement(Duration.ofSeconds(5))
                .doOnNext(house -> log.info(">>> Данные из БД ПОЛУЧЕНЫ в потоке: {}", Thread.currentThread().getName()));
    }

    @Override
    public Flux<HouseResponse> findAll() {
        String sql = "SELECT * FROM houses_view";

        log.info(">>> Начинаем обращение к БД в потоке: {}", Thread.currentThread().getName());

        return databaseClient.sql(sql)
                .map((row, metadata) -> mapRow(row))
                .all()
                .delayElements(Duration.ofMillis(500))
                .doOnNext(house -> log.info(">>> Данные из БД ПОЛУЧЕНЫ в потоке: {}", Thread.currentThread().getName()));
    }

    private HouseResponse mapRow(Row row) {
        return new HouseResponse(
                row.get("house_id", UUID.class),
                row.get("address", String.class),
                row.get("price", BigDecimal.class),
                row.get("status", String.class),
                row.get("owner_id", UUID.class)
        );
    }
}