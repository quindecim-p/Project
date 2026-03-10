package com.example.infrastructure.adapters.kafka;

import com.example.application.usecases.DepositMoneyUseCase;
import com.example.application.usecases.WithdrawMoneyUseCase;
import com.example.infrastructure.adapters.kafka.dto.PaymentCommandMessage;
import com.example.infrastructure.adapters.kafka.dto.PaymentResponseMessage;
import com.example.infrastructure.adapters.outbox.OutboxEvent;
import com.example.infrastructure.adapters.outbox.OutboxRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Duration;

@Slf4j
@Component
@RequiredArgsConstructor
public class BuyerKafkaConsumer {

    private final WithdrawMoneyUseCase withdrawMoneyUseCase;
    private final DepositMoneyUseCase depositMoneyUseCase;
    private final StringRedisTemplate redisTemplate;
    private final OutboxRepository outboxRepository;
    private final ObjectMapper mapper;

    private final TransactionTemplate transactionTemplate;

    @KafkaListener(topics = "payment-commands", groupId = "buyer-saga-group")
    @SneakyThrows
    public void processPaymentCommand(String messageJson) {
        var command = mapper.readValue(messageJson, PaymentCommandMessage.class);
        String txId = command.transactionId().toString();

        if ("WITHDRAW".equals(command.action())) {
            processWithdrawal(command, txId);
        } else if ("COMPENSATE".equals(command.action())) {
            handleCompensate(command, txId);
        }
    }

    @SneakyThrows
    public void processWithdrawal(PaymentCommandMessage cmd, String txId) {
        String withdrawKey = "tx:withdraw:" + txId;
        String compKey = "tx:comp:" + txId;

        if (Boolean.TRUE.equals(redisTemplate.hasKey(compKey))) {
            log.warn("Списание отклонено: Транзакция {} отменена!", txId);
            saveToOutbox(cmd, txId, false, "Транзакция уже отменена");
            return;
        }

        Boolean isNew = redisTemplate.opsForValue().setIfAbsent(withdrawKey, "processed", Duration.ofDays(1));
        if (Boolean.FALSE.equals(isNew)) {
            log.info("Повторный запрос списания {}.", txId);
            return;
        }

        try {
            transactionTemplate.executeWithoutResult(status -> {
                withdrawMoneyUseCase.execute(cmd.buyerId(), cmd.amount());

                if (true) {
                    throw new RuntimeException("Ошибка в сервисе после сохранения в бд");
                }

                saveToOutbox(cmd, txId, true, "");
            });

        } catch (Exception e) {
            redisTemplate.delete(withdrawKey);
            log.error("Ошибка при списании: ", e);

            saveToOutbox(cmd, txId, false, e.getMessage());
        }
    }

    @SneakyThrows
    private void saveToOutbox(PaymentCommandMessage cmd, String txId, boolean isSuccess, String errorMsg) {
        var response = new PaymentResponseMessage(
                cmd.transactionId(), cmd.houseId(), cmd.buyerId(), isSuccess, errorMsg
        );

        OutboxEvent event = new OutboxEvent();
        event.setTopic("payment-responses");
        event.setMessageKey(txId);
        event.setPayload(mapper.writeValueAsString(response));

        outboxRepository.save(event);
    }

    private void handleCompensate(PaymentCommandMessage cmd, String txId) {
        String compKey = "tx:comp:" + txId;
        String withdrawKey = "tx:withdraw:" + txId;

        Boolean isNew = redisTemplate.opsForValue().setIfAbsent(compKey, "processed", Duration.ofDays(1));
        if (Boolean.FALSE.equals(isNew)) {
            log.info("Повторный запрос компенсации {}.", txId);
            return;
        }

        if (Boolean.FALSE.equals(redisTemplate.hasKey(withdrawKey))) {
            log.warn("Попытка компенсировать несписанные деньги! Списание {} не найдено.", txId);
            return;
        }

        boolean isDbUpdated = false;
        try {
            depositMoneyUseCase.execute(cmd.buyerId(), cmd.amount());
        } catch (Exception e) {
            if (!isDbUpdated) {
                redisTemplate.delete(compKey);
            }
            log.error("Ошибка при компенсации: ", e);
        }
    }
}