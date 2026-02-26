package com.example.infrastructure.persistence;

import com.example.domain.model.events.DomainEvent;
import com.example.domain.ports.EventStore;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Repository;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Repository
@Profile("file")
@RequiredArgsConstructor
public class FileEventStore implements EventStore {

    private final ObjectMapper mapper;
    private final KafkaTemplate<String, String> kafka;

    @Value("${app.file-storage.path}")
    private String filePath;

    @Override
    @SneakyThrows
    public void save(UUID aggregateId, List<DomainEvent> events) {
        File file = new File(filePath);

        if (!file.exists()) {
            boolean created = file.createNewFile();
            if (!created) throw new IOException("Не удалось создать файл базы событий");
        }

        for (DomainEvent event : events) {
            String type = event.getClass().getName();
            String payload = mapper.writeValueAsString(event);

            FileEntry entry = new FileEntry(aggregateId, type, payload);

            String jsonLine = mapper.writeValueAsString(entry);

            Files.writeString(file.toPath(), jsonLine + "\n", StandardOpenOption.APPEND);

            kafka.send("domain-events", aggregateId.toString(),
                    mapper.writeValueAsString(new KafkaWrapper(event.getClass().getSimpleName(), payload)));
        }
    }

    @Override
    @SneakyThrows
    public List<DomainEvent> load(UUID aggregateId) {
        File file = new File(filePath);

        if (!file.exists()) return List.of();

        try (var lines = Files.lines(file.toPath())) { // Stream<String>
            return lines
                    .map(this::parseLine)
                    .filter(entry -> entry != null && entry.aggregateId.equals(aggregateId))
                    .map(this::deserializeEvent)
                    .collect(Collectors.toList());
        }
    }

    @SneakyThrows
    private FileEntry parseLine(String line) {
        try {
            return mapper.readValue(line, FileEntry.class);
        } catch (Exception e) {
            return null;
        }
    }

    @SneakyThrows
    private DomainEvent deserializeEvent(FileEntry entry) {
        Class<?> clazz = Class.forName(entry.type);
        return (DomainEvent) mapper.readValue(entry.payload, clazz);
    }

    public record FileEntry(UUID aggregateId, String type, String payload) {}
    public record KafkaWrapper(String type, String payload) {}
}
