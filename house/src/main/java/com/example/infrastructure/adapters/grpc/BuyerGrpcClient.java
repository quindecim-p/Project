package com.example.infrastructure.adapters.grpc;

import com.example.domain.model.exceptions.BusinessException;
import com.example.domain.model.exceptions.ServiceUnavailableException;
import com.example.domain.ports.BuyerPort;
import com.example.grpc.BuyerGrpcServiceGrpc;
import com.example.grpc.PaymentRequest;
import com.example.grpc.PaymentResponse;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.UUID;

@Service
public class BuyerGrpcClient implements BuyerPort {

    @GrpcClient("buyer-service")
    private BuyerGrpcServiceGrpc.BuyerGrpcServiceBlockingStub stub;

    @Override
    @CircuitBreaker(name = "banking", fallbackMethod = "paymentFallback")
    public void requestPayment(UUID buyerId, BigDecimal amount) {
        PaymentRequest request = PaymentRequest.newBuilder()
                .setBuyerId(buyerId.toString())
                .setAmount(amount.toString())
                .build();

        PaymentResponse response = stub.withdraw(request);

        if (!response.getSuccess()) {
            throw new BusinessException("Платеж отклонен: " + response.getMessage());
        }
    }

    public void paymentFallback(UUID buyerId, BigDecimal amount, Throwable t) {
        System.err.println("--> Fallback сработал! Причина: " + t.getMessage());
        throw new ServiceUnavailableException(t.getMessage());
    }


}
