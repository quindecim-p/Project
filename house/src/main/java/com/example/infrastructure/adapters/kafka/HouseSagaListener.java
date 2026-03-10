package com.example.infrastructure.adapters.kafka;

import com.example.application.sagas.AsyncBuyHouseSaga;
import com.example.infrastructure.adapters.kafka.dto.PaymentResponseMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class HouseSagaListener {

    private final AsyncBuyHouseSaga asyncBuyHouseSaga;
    private final ObjectMapper mapper;

    @KafkaListener(topics = "payment-responses", groupId = "house-saga-group")
    @SneakyThrows
    public void onPaymentResponse(String messageJson) {
        var response = mapper.readValue(messageJson, PaymentResponseMessage.class);

        asyncBuyHouseSaga.onPaymentResult(
                response.transactionId(),
                response.houseId(),
                response.buyerId(),
                response.success()
        );
    }
}