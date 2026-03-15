package com.example.infrastructure.adapters;

import com.example.application.ports.ExternalValidationPort;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class ExternalValidationAdapter implements ExternalValidationPort {

    @Override
    public void checkHouseEncumbrances(UUID houseId) {
        try {
            System.out.println("Начинаю проверку дома " + houseId + " (займет 3 секунды)...");
            //Thread.sleep(15000);
            Thread.sleep(3000);
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
            System.out.println("Покупатель " + buyerId + " проверен!");
            //throw new RuntimeException("У покупателя арестованы счета!");
        } catch (InterruptedException e) {
            System.err.println("ЗАПРОС ПРОВЕРКИ ПОКУПАТЕЛЯ ПРЕРВАН!");
            Thread.currentThread().interrupt();
        }
    }

}
