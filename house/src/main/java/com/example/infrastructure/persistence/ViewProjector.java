package com.example.infrastructure.persistence;

import com.example.domain.model.events.HouseCreated;
import com.example.domain.model.events.HouseDeleted;
import com.example.domain.model.events.HouseSold;
import com.example.domain.model.events.HouseUpdated;
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

    @KafkaListener(topics = "domain-events", groupId = "house-group")
    @SneakyThrows
    public void onEvent(String message) {
        var wrapper = mapper.readValue(message, PostgresEventStore.KafkaWrapper.class);
        String json = wrapper.payload();

        UUID houseIdToEvict = null;

        switch (wrapper.type()) {
            case "HouseCreated" -> {
                var e = mapper.readValue(json, HouseCreated.class);
                jdbc.update("INSERT INTO houses_view (house_id, address, price, status) VALUES (?, ?, ?, 'FOR_SALE')",
                        e.houseId(), e.address(), e.price());

                houseIdToEvict = e.houseId();
            }

            case "HouseSold" -> {
                var e = mapper.readValue(json, HouseSold.class);
                jdbc.update("UPDATE houses_view SET status = 'SOLD', owner_id = ? WHERE house_id = ?",
                        e.newOwnerId(), e.houseId());

                houseIdToEvict = e.houseId();
            }

            case "HouseUpdated" -> {
                var e = mapper.readValue(json, HouseUpdated.class);
                jdbc.update("UPDATE houses_view SET address = ?, price = ? WHERE house_id = ?",
                        e.address(), e.price(), e.houseId());
                houseIdToEvict = e.houseId();
            }

            case "HouseDeleted" -> {
                var e = mapper.readValue(json, HouseDeleted.class);
                jdbc.update("DELETE FROM houses_view WHERE house_id = ?", e.houseId());
                houseIdToEvict = e.houseId();
            }
        }

        if (houseIdToEvict != null) {
            var cache = cacheManager.getCache("houses");
            if (cache != null) {
                cache.evict(houseIdToEvict);
            }
        }
    }

}
