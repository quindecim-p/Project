package com.example.infrastructure.adapters.grps;

import com.example.application.usecases.DepositMoneyUseCase;
import com.example.application.usecases.WithdrawMoneyUseCase;
import com.example.grpc.BuyerGrpcServiceGrpc;
import com.example.grpc.PaymentRequest;
import com.example.grpc.PaymentResponse;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

@Slf4j
@GrpcService
@RequiredArgsConstructor
public class BuyerGrpcServer extends BuyerGrpcServiceGrpc.BuyerGrpcServiceImplBase {

    private final WithdrawMoneyUseCase withdrawMoneyUseCase;
    private final DepositMoneyUseCase depositMoneyUseCase;
    private final StringRedisTemplate redisTemplate;

    @Override
    public void withdraw(PaymentRequest request, StreamObserver<PaymentResponse> responseObserver) {
//        if (true) {
//            throw new RuntimeException("Ошибка в сервисе перед внесением изменений в БД");
//        }

        String txId = request.getIdempotencyKey();
        String withdrawKey = "tx:withdraw:" + txId;
        String compKey = "tx:comp:" + txId;

        log.info("GRPC: Запрос на списание. TxID: {}, Buyer: {}", txId, request.getBuyerId());

        if (Boolean.TRUE.equals(redisTemplate.hasKey(compKey))) {
            log.warn("Списание отклонено: Транзакция {} уже была отменена!", txId);
            sendResponse(responseObserver, false, "Транзакция уже отменена");
            return;
        }

        Boolean isNewWithdrawal = redisTemplate.opsForValue().setIfAbsent(withdrawKey, "processed", Duration.ofDays(1));
        if (Boolean.FALSE.equals(isNewWithdrawal)) {
            log.info("Повторный запрос списания {}. Возвращаем SUCCESS.", txId);
            sendResponse(responseObserver, true, "");
            return;
        }

        boolean isDbUpdated = false;

        try {
            UUID buyerId = UUID.fromString(request.getBuyerId());
            BigDecimal amount = new BigDecimal(request.getAmount());

            auditLog(buyerId, amount, txId, "WITHDRAW");
            withdrawMoneyUseCase.execute(buyerId, amount);
            isDbUpdated = true;

//            if (true) {
//                throw new RuntimeException("Ошибка в сервисе после внесения изменений в БД");
//            }

            sendResponse(responseObserver, true, "");
        } catch (Exception e) {
            if (!isDbUpdated) {
                redisTemplate.delete(withdrawKey);
            }
            log.error("Ошибка при списании: ", e);
            sendResponse(responseObserver, false, e.getMessage());
        }
    }

    @Override
    public void deposit(PaymentRequest request, StreamObserver<PaymentResponse> responseObserver) {
        String txId = request.getIdempotencyKey();
        String withdrawKey = "tx:withdraw:" + txId;
        String compKey = "tx:comp:" + txId;

        log.info("GRPC: Запрос на КОМПЕНСАЦИЮ. TxID: {}, Buyer: {}", txId, request.getBuyerId());

        Boolean isNewCompensation = redisTemplate.opsForValue().setIfAbsent(compKey, "processed", Duration.ofDays(1));
        if (Boolean.FALSE.equals(isNewCompensation)) {
            log.info("Повторный запрос компенсации {}. Возвращаем SUCCESS.", txId);
            sendResponse(responseObserver, true, "");
            return;
        }

        if (Boolean.FALSE.equals(redisTemplate.hasKey(withdrawKey))) {
            log.warn("Попытка компенсировать несписанные деньги! Списание {} не найдено.", txId);
            sendResponse(responseObserver, true, "");
            return;
        }

        boolean isDbUpdated = false;

        try {
            UUID buyerId = UUID.fromString(request.getBuyerId());
            BigDecimal amount = new BigDecimal(request.getAmount());

            auditLog(buyerId, amount, txId, "COMPENSATE (DEPOSIT)");
            depositMoneyUseCase.execute(buyerId, amount);
            isDbUpdated = true;

            sendResponse(responseObserver, true, "");
        } catch (Exception e) {
            if (!isDbUpdated) {
                redisTemplate.delete(compKey);
            }
            log.error("Ошибка при компенсации: ", e);
            sendResponse(responseObserver, false, e.getMessage());
        }
    }

    private void sendResponse(StreamObserver<PaymentResponse> responseObserver, boolean isSuccess, String message) {
        PaymentResponse response = PaymentResponse.newBuilder()
                .setSuccess(isSuccess)
                .setMessage(message != null ? message : "")
                .build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    private void auditLog(UUID buyerId, BigDecimal amount, String txId, String action) {
        log.info("[Time: {}] Action: {}, Buyer ID: {}, TxKey: {}, Amount: {}",
                Instant.now(), action, buyerId, txId, amount);
    }
}