package com.example.infrastructure.persistence.query;

import com.example.application.ports.HouseQueryService;
import com.example.infrastructure.web.dto.response.HouseResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SqlHouseQueryService implements HouseQueryService {

    private final JdbcTemplate jdbc;

    private final RowMapper<HouseResponse> houseRowMapper = (rs, rowNum) -> {
        String ownerIdStr = rs.getString("owner_id");
        return new HouseResponse(
                UUID.fromString(rs.getString("house_id")),
                rs.getString("address"),
                rs.getBigDecimal("price"),
                rs.getString("status"),
                ownerIdStr != null ? UUID.fromString(ownerIdStr) : null
        );
    };

    @Override
    @Cacheable(value = "houses", key = "#id")
    public Optional<HouseResponse> findById(UUID id) {
        System.out.println("======> Выполняем реальный запрос в БД для ID: " + id);
        String sql = "SELECT * FROM houses_view WHERE house_id = ?";
        try {
            return Optional.ofNullable(jdbc.queryForObject(sql, houseRowMapper, id));
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    @Override
    public List<HouseResponse> findAll() {
        String sql = "SELECT * FROM houses_view, pg_sleep(0.5)";
        return jdbc.query(sql, houseRowMapper);
    }
}
