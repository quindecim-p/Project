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

import java.math.BigDecimal;
import java.util.UUID;

@Slf4j
@GrpcService
@RequiredArgsConstructor
public class BuyerGrpcServer extends BuyerGrpcServiceGrpc.BuyerGrpcServiceImplBase {

    private final WithdrawMoneyUseCase withdrawMoneyUseCase;
    private final DepositMoneyUseCase depositMoneyUseCase;

    @Override
    public void withdraw(PaymentRequest request, StreamObserver<PaymentResponse> responseObserver) {
        log.info("GRPC: Запрос на списание средств у {}", request.getBuyerId());
        handleTransaction(request, responseObserver, withdrawMoneyUseCase::execute);
    }

    @Override
    public void deposit(PaymentRequest request, StreamObserver<PaymentResponse> responseObserver) {
        log.info("GRPC COMPENSATE: Запрос на возврат средств покупателю {}", request.getBuyerId());
        handleTransaction(request, responseObserver, depositMoneyUseCase::execute);
    }

    private void handleTransaction(PaymentRequest request,
                                   StreamObserver<PaymentResponse> responseObserver,
                                   TransactionExecutor executor) {
        try {
            UUID buyerId = UUID.fromString(request.getBuyerId());
            BigDecimal amount = new BigDecimal(request.getAmount());

            executor.execute(buyerId, amount);

            PaymentResponse response = PaymentResponse.newBuilder()
                    .setSuccess(true)
                    .build();

            responseObserver.onNext(response);
        } catch (Exception e) {
            log.error("GRPC Error: ", e);
            PaymentResponse response = PaymentResponse.newBuilder()
                    .setSuccess(false)
                    .setMessage(e.getMessage())
                    .build();

            responseObserver.onNext(response);
        }
        responseObserver.onCompleted();
    }

    @FunctionalInterface
    interface TransactionExecutor {
        void execute(UUID id, BigDecimal amount);
    }
}