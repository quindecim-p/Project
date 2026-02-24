package com.example.infrastructure.persistence;

import com.example.domain.model.events.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.cache.CacheManager;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ViewProjector {

    private final JdbcTemplate jdbc;
    private final ObjectMapper mapper;
    private final CacheManager cacheManager;

    @KafkaListener(topics = "domain-events", groupId = "buyer-group")
    @SneakyThrows
    public void onEvent(String message) {
        var wrapper = mapper.readValue(message, PostgresEventStore.KafkaWrapper.class);
        String json = wrapper.payload();

        UUID buyerIdToEvict = null;

        switch (wrapper.type()) {
            case "BuyerCreated" -> {
                var e = mapper.readValue(json, BuyerCreated.class);
                jdbc.update("INSERT INTO buyers_view (buyer_id, name, balance) VALUES (?, ?, 0)",
                        e.buyerId(), e.name());

                buyerIdToEvict = e.buyerId();
            }

            case "MoneyDeposited" -> {
                var e = mapper.readValue(json, MoneyDeposited.class);
                jdbc.update("UPDATE buyers_view SET balance = balance + ? WHERE buyer_id = ?",
                        e.amount(), e.buyerId());

                buyerIdToEvict = e.buyerId();
            }

            case "MoneyWithdrawn" -> {
                var e = mapper.readValue(json, MoneyWithdrawn.class);
                jdbc.update("UPDATE buyers_view SET balance = balance - ? WHERE buyer_id = ?",
                        e.amount(), e.buyerId());

                buyerIdToEvict = e.buyerId();
            }

            case "BuyerNameChanged" -> {
                var e = mapper.readValue(json, BuyerNameChanged.class);
                jdbc.update("UPDATE buyers_view SET name = ? WHERE buyer_id = ?",
                        e.newName(), e.buyerId());
                buyerIdToEvict = e.buyerId();
            }

            case "BuyerDeleted" -> {
                var e = mapper.readValue(json, BuyerDeleted.class);
                jdbc.update("DELETE FROM buyers_view WHERE buyer_id = ?", e.buyerId());
                buyerIdToEvict = e.buyerId();
            }

        }

        if (buyerIdToEvict != null) {
            var cache = cacheManager.getCache("buyers");
            if (cache != null) {
                cache.evict(buyerIdToEvict);
            }
        }
    }

}
