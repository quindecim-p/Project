package com.example.infrastructure.adapters.outbox;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class OutboxRelayScheduler {

    private final OutboxRepository outboxRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;

    @Scheduled(fixedDelay = 500)
    public void processOutboxEvents() {
        List<OutboxEvent> events = outboxRepository.findAll();

        for (OutboxEvent event : events) {
            try {
                kafkaTemplate.send(event.getTopic(), event.getMessageKey(), event.getPayload()).get();
                outboxRepository.delete(event);
            } catch (Exception e) {
                log.error("Ошибка отправки в Kafka", e);
                break;
            }
        }
    }
}