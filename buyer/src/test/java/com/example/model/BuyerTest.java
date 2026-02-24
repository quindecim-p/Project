package com.example.model;

import com.example.domain.model.Buyer;
import com.example.domain.model.exceptions.InsufficientFundsException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class BuyerTest {

    @Test
    @DisplayName("Баланс должен увеличиваться при пополнении")
    void deposit_ShouldIncreaseBalance_WhenDeposit() {
        Buyer buyer = new Buyer("Ivan");

        buyer.deposit(new BigDecimal("100"));

        assertEquals(new BigDecimal("100"), buyer.getBalance());
    }

    @Test
    @DisplayName("Должна выбрасываться ошибка, если денег не хватает")
    void pay_ShouldThrowException_WhenInsufficientFunds() {
        Buyer buyer = new Buyer( "Ivan");

        buyer.deposit(new BigDecimal("50"));

        assertThrows(InsufficientFundsException.class, () -> buyer.pay(new BigDecimal("100")));
    }

}