package com.example.infrastructure.persistence.query;

import com.example.application.ports.BuyerQueryService;
import com.example.infrastructure.web.dto.response.BuyerResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Profile("!file")
@RequiredArgsConstructor
public class SqlBuyerQueryService implements BuyerQueryService {

    private final JdbcTemplate jdbc;

    private final RowMapper<BuyerResponse> buyerRowMapper = (rs, rowNum) -> new BuyerResponse(
            UUID.fromString(rs.getString("buyer_id")),
            rs.getString("name"),
            rs.getBigDecimal("balance")
    );

    @Override
    @Cacheable(value = "buyers", key = "#id")
    public Optional<BuyerResponse> findById(UUID id) {
        System.out.println("======> Выполняем реальный запрос в БД для ID: " + id);
        String sql = "SELECT * FROM buyers_view WHERE buyer_id = ?";
        try {
            return Optional.ofNullable(jdbc.queryForObject(sql, buyerRowMapper, id));
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    @Override
    public List<BuyerResponse> findAll() {
        String sql = "SELECT * FROM buyers_view";
        return jdbc.query(sql, buyerRowMapper);
    }

}
