package com.example.infrastructure.adapters.kafka;

import com.example.domain.ports.BuyerPort;
import com.example.infrastructure.adapters.kafka.dto.PaymentCommandMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Profile("Kafka")
public class BuyerKafkaProducer implements BuyerPort {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper mapper;

    @Override
    @SneakyThrows
    public void requestPayment(UUID houseId, UUID buyerId, BigDecimal amount, UUID transactionId) {
//        if (true) {
//            throw new RuntimeException("Ошибка перед отправкой запроса в другой сервис");
//        }

        var cmd = new PaymentCommandMessage(transactionId, houseId, buyerId, amount, "WITHDRAW");
        kafkaTemplate.send("payment-commands", transactionId.toString(), mapper.writeValueAsString(cmd));
    }

    @Override
    @SneakyThrows
    public void compensatePayment(UUID houseId, UUID buyerId, BigDecimal amount, UUID transactionId) {
        var cmd = new PaymentCommandMessage(transactionId, houseId, buyerId, amount, "COMPENSATE");
        kafkaTemplate.send("payment-commands", transactionId.toString(), mapper.writeValueAsString(cmd));
    }
}