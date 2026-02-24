package com.example.infrastructure.config;

import com.example.application.usecases.*;
import com.example.domain.ports.EventStore;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DomainConfig {

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }

    @Bean
    public CreateBuyerUseCase createBuyerUseCase(EventStore store) {
        return new CreateBuyerUseCase(store);
    }

    @Bean
    public UpdateBuyerUseCase updateBuyerUseCase(EventStore store) {
        return new UpdateBuyerUseCase(store);
    }

    @Bean
    public DeleteBuyerUseCase deleteBuyerUseCase(EventStore store) {
        return new DeleteBuyerUseCase(store);
    }

    @Bean
    public DepositMoneyUseCase depositMoneyUseCase(EventStore store) {
        return new DepositMoneyUseCase(store);
    }

    @Bean
    public WithdrawMoneyUseCase withdrawMoneyUseCase(EventStore store) { return new WithdrawMoneyUseCase(store); }

}
