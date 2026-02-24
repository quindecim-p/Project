package com.example.infrastructure.persistence.query;

import com.example.application.ports.BuyerQueryService;
import com.example.infrastructure.persistence.FileEventStore.FileEntry; // используем твой рекорд
import com.example.infrastructure.web.dto.response.BuyerResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.io.File;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Profile("file")
@RequiredArgsConstructor
public class FileBuyerQueryService implements BuyerQueryService {

    private final ObjectMapper mapper;

    @Value("${app.file-storage.path}")
    private String filePath;

    @Override
    public Optional<BuyerResponse> findById(UUID id) {
        return findAll().stream()
                .filter(b -> b.id().equals(id))
                .findFirst();
    }

    @Override
    @SneakyThrows
    public List<BuyerResponse> findAll() {
        File file = new File(filePath);
        if (!file.exists()) return List.of();

        Map<UUID, BuyerState> states = new HashMap<>();

        try (var lines = Files.lines(file.toPath())) {
            lines.forEach(line -> {
                FileEntry entry = parseLine(line);
                if (entry == null) return;

                states.compute(entry.aggregateId(), (id, current) -> {
                    BigDecimal balance = (current == null) ? BigDecimal.ZERO : current.balance;
                    String name = (current == null) ? "" : current.name;

                    if (entry.type().endsWith("BuyerCreated")) {
                        name = extractName(entry.payload());
                    } else if (entry.type().endsWith("MoneyDeposited")) {
                        balance = balance.add(extractAmount(entry.payload()));
                    } else if (entry.type().endsWith("MoneyWithdrawn")) {
                        balance = balance.subtract(extractAmount(entry.payload()));
                    }
                    return new BuyerState(name, balance);
                });
            });
        }

        return states.entrySet().stream()
                .map(e -> new BuyerResponse(e.getKey(), e.getValue().name, e.getValue().balance))
                .collect(Collectors.toList());
    }

    @SneakyThrows
    private FileEntry parseLine(String line) {
        try {
            return mapper.readValue(line, FileEntry.class);
        }
        catch (Exception e) {
            return null;
        }
    }

    @SneakyThrows
    private String extractName(String payload) {
        return mapper.readTree(payload).get("name").asText();
    }

    @SneakyThrows
    private BigDecimal extractAmount(String payload) {
        return new BigDecimal(mapper.readTree(payload).get("amount").asText());
    }

    private record BuyerState(String name, BigDecimal balance) {}
}
