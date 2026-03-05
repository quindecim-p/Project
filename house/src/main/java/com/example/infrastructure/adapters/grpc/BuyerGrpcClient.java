package com.example.infrastructure.adapters.grpc;

import com.example.domain.model.exceptions.BusinessException;
import com.example.domain.model.exceptions.ServiceUnavailableException;
import com.example.domain.ports.BuyerPort;
import com.example.grpc.BuyerGrpcServiceGrpc;
import com.example.grpc.PaymentRequest;
import com.example.grpc.PaymentResponse;
import io.grpc.StatusRuntimeException;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class BuyerGrpcClient implements BuyerPort {

    @GrpcClient("buyer-service")
    private BuyerGrpcServiceGrpc.BuyerGrpcServiceBlockingStub stub;

    @Override
    public void requestPayment(UUID buyerId, BigDecimal amount, UUID transactionId) {
        PaymentRequest request = PaymentRequest.newBuilder()
                .setBuyerId(buyerId.toString())
                .setAmount(amount.toString())
                .setIdempotencyKey(transactionId.toString())
                .build();

//        if (true) {
//            throw new RuntimeException("Ошибка перед запросом в другой сервис");
//        }

        PaymentResponse response;
        try {
            response = stub.withDeadlineAfter(5, TimeUnit.SECONDS).withdraw(request);
        } catch (StatusRuntimeException e) {
            throw new ServiceUnavailableException("Сетевая ошибка при списании");
        }

        if (!response.getSuccess()) {
            throw new BusinessException("Платеж отклонен: " + response.getMessage());
        }
    }

    @Override
    public void compensatePayment(UUID buyerId, BigDecimal amount, UUID transactionId) {
        PaymentRequest request = PaymentRequest.newBuilder()
                .setBuyerId(buyerId.toString())
                .setAmount(amount.toString())
                .setIdempotencyKey(transactionId.toString())
                .build();

//        if (true) {
//            throw new RuntimeException("Ошибка перед запросом в другой сервис на компенсацию");
//        }

        try {
            PaymentResponse response = stub.withDeadlineAfter(5, TimeUnit.SECONDS).deposit(request);
            if (!response.getSuccess()) {
                throw new RuntimeException("Банк ответил отказом на компенсацию: " + response.getMessage());
            }
        } catch (Exception e) {
            throw new RuntimeException("Ошибка соединения при возврате средств", e);
        }
    }
}