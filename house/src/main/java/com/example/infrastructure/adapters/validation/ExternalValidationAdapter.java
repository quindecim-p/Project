package com.example.infrastructure.adapters.validation;

import com.example.application.ports.ExternalValidationPort;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class ExternalValidationAdapter implements ExternalValidationPort {

    @Override
    public void checkHouseEncumbrances(UUID houseId) {
        try {
            System.out.println("Начинаю проверку дома " + houseId + " (займет 5 сек)...");
            Thread.sleep(5000);
            System.out.println("Дом " + houseId + " чист!");
        } catch (InterruptedException e) {
            System.err.println("ЗАПРОС ПРОВЕРКИ ДОМА ПРЕРВАН!");
            Thread.currentThread().interrupt();
        }
    }

    @Override
    public void checkBuyerSolvency(UUID buyerId) {
        try {
            System.out.println("Проверяю покупателя " + buyerId + "...");
            Thread.sleep(1000);
            throw new RuntimeException("У покупателя арестованы счета!");
        } catch (InterruptedException e) {
            System.err.println("ЗАПРОС ПРОВЕРКИ ПОКУПАТЕЛЯ ПРЕРВАН!");
            Thread.currentThread().interrupt();
        }
    }

}
