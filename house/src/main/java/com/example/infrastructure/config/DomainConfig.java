package com.example.infrastructure.config;

import com.example.application.ports.ExternalValidationPort;
import com.example.application.sagas.BuyHouseSaga;
import com.example.application.usecases.BuyHouseUseCase;
import com.example.application.usecases.CreateHouseUseCase;
import com.example.application.usecases.DeleteHouseUseCase;
import com.example.application.usecases.UpdateHouseUseCase;
import com.example.domain.ports.BuyerPort;
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
    public CreateHouseUseCase createHouseUseCase(EventStore store) {
        return new CreateHouseUseCase(store);
    }

    @Bean
    public UpdateHouseUseCase updateHouseUseCase(EventStore store) {
        return new UpdateHouseUseCase(store);
    }

    @Bean
    public DeleteHouseUseCase deleteHouseUseCase(EventStore store) {
        return new DeleteHouseUseCase(store);
    }

    @Bean
    public BuyHouseUseCase buyHouseUseCase(EventStore store) {
        return new BuyHouseUseCase(store);
    }

    @Bean
    public BuyHouseSaga buyHouseSaga(EventStore eventStore,
                                     BuyerPort buyerPort,
                                     BuyHouseUseCase buyHouseUseCase,
                                     ExternalValidationPort externalValidationPort) {
        return new BuyHouseSaga(eventStore, buyerPort, buyHouseUseCase, externalValidationPort);
    }

}
