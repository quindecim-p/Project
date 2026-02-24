package com.example.infrastructure.adapters.grps;

import com.example.application.usecases.WithdrawMoneyUseCase;
import com.example.grpc.BuyerGrpcServiceGrpc;
import com.example.grpc.PaymentRequest;
import com.example.grpc.PaymentResponse;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import net.devh.boot.grpc.server.service.GrpcService;

import java.math.BigDecimal;
import java.util.UUID;

@GrpcService
@RequiredArgsConstructor
public class BuyerGrpcServer extends BuyerGrpcServiceGrpc.BuyerGrpcServiceImplBase {

    private final WithdrawMoneyUseCase withdrawMoneyUseCase;

    @Override
    public void withdraw(PaymentRequest request, StreamObserver<PaymentResponse> responseObserver) {
        try {
            UUID buyerId = UUID.fromString(request.getBuyerId());
            BigDecimal amount = new BigDecimal(request.getAmount());

            withdrawMoneyUseCase.execute(buyerId, amount);

            PaymentResponse response = PaymentResponse.newBuilder()
                    .setSuccess(true)
                    .build();

            responseObserver.onNext(response);
        } catch (Exception e) {
            PaymentResponse response = PaymentResponse.newBuilder()
                    .setSuccess(false)
                    .setMessage(e.getMessage())
                    .build();

            responseObserver.onNext(response);
        }

        responseObserver.onCompleted();
    }

}
