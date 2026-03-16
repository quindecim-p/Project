package com.example.domain.model;

import com.example.domain.model.events.*;
import com.example.domain.model.exceptions.InsufficientFundsException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class BuyerTest {

    @Test
    void shouldCreateBuyerWithZeroBalance() {
        Buyer buyer = new Buyer("Name");

        assertNotNull(buyer.getId());
        assertEquals("Name", buyer.getName());
        assertEquals(BigDecimal.ZERO, buyer.getBalance());
        assertFalse(buyer.isDeleted());

        assertEquals(1, buyer.getUncommitedChanges().size());
        assertInstanceOf(BuyerCreated.class, buyer.getUncommitedChanges().getFirst());
    }

    @Test
    void shouldRestoreFromHistoryAndCalculateBalance() {
        UUID buyerId = UUID.randomUUID();
        List<DomainEvent> history = List.of(
                new BuyerCreated(buyerId, "Name"),
                new MoneyDeposited(buyerId, new BigDecimal("1000")),
                new MoneyWithdrawn(buyerId, new BigDecimal("300"))
        );

        Buyer buyer = new Buyer(buyerId, history);

        assertEquals("Name", buyer.getName());
        assertEquals(new BigDecimal("700"), buyer.getBalance());
        assertEquals(0, buyer.getUncommitedChanges().size());
    }

    @Test
    void shouldDepositMoneySuccessfully() {
        UUID buyerId = UUID.randomUUID();
        Buyer buyer = new Buyer(buyerId, List.of(new BuyerCreated(buyerId, "Name")));

        buyer.deposit(new BigDecimal("500"));

        assertEquals(new BigDecimal("500"), buyer.getBalance());
        assertEquals(1, buyer.getUncommitedChanges().size());
        assertInstanceOf(MoneyDeposited.class, buyer.getUncommitedChanges().getFirst());
    }

    @Test
    void shouldWithdrawMoneySuccessfully() {
        UUID buyerId = UUID.randomUUID();
        Buyer buyer = new Buyer(buyerId, List.of(
                new BuyerCreated(buyerId, "Name"),
                new MoneyDeposited(buyerId, new BigDecimal("1000"))
        ));

        buyer.withdraw(new BigDecimal("400"));

        assertEquals(new BigDecimal("600"), buyer.getBalance());
        assertEquals(1, buyer.getUncommitedChanges().size());
        assertInstanceOf(MoneyWithdrawn.class, buyer.getUncommitedChanges().getFirst());
    }

    @Test
    void shouldPaySuccessfully() {
        UUID buyerId = UUID.randomUUID();
        Buyer buyer = new Buyer(buyerId, List.of(
                new BuyerCreated(buyerId, "Name"),
                new MoneyDeposited(buyerId, new BigDecimal("1000"))
        ));

        buyer.pay(new BigDecimal("1000"));

        assertEquals(BigDecimal.ZERO, buyer.getBalance());
        assertEquals(1, buyer.getUncommitedChanges().size());
        assertInstanceOf(MoneyWithdrawn.class, buyer.getUncommitedChanges().getFirst());
    }

    @Test
    void shouldThrowExceptionWhenInsufficientFunds() {
        UUID buyerId = UUID.randomUUID();
        Buyer buyer = new Buyer(buyerId, List.of(
                new BuyerCreated(buyerId, "Name"),
                new MoneyDeposited(buyerId, new BigDecimal("100"))
        ));

        InsufficientFundsException exception = assertThrows(InsufficientFundsException.class,
                () -> buyer.withdraw(new BigDecimal("500")));

        assertTrue(exception.getMessage().contains("недостаточно денег"));
        assertEquals(new BigDecimal("100"), buyer.getBalance());
    }

    @Test
    void shouldUpdateNameSuccessfully() {
        UUID buyerId = UUID.randomUUID();
        Buyer buyer = new Buyer(buyerId, List.of(new BuyerCreated(buyerId, "OldName")));

        buyer.updateName("NewName");

        assertEquals("NewName", buyer.getName());
        assertEquals(1, buyer.getUncommitedChanges().size());
        BuyerNameChanged event = (BuyerNameChanged) buyer.getUncommitedChanges().getFirst();
        assertEquals("NewName", event.newName());
    }

    @Test
    void shouldNotGenerateEventIfNameIsSame() {
        UUID buyerId = UUID.randomUUID();
        Buyer buyer = new Buyer(buyerId, List.of(new BuyerCreated(buyerId, "Name")));

        buyer.updateName("Name");

        assertTrue(buyer.getUncommitedChanges().isEmpty());
    }

    @Test
    void shouldDeleteBuyerSuccessfully() {
        UUID buyerId = UUID.randomUUID();
        Buyer buyer = new Buyer(buyerId, List.of(new BuyerCreated(buyerId, "Name")));

        buyer.delete();

        assertTrue(buyer.isDeleted());
        assertEquals(1, buyer.getUncommitedChanges().size());
        assertInstanceOf(BuyerDeleted.class, buyer.getUncommitedChanges().getFirst());
    }

    @Test
    void shouldThrowExceptionWhenOperatingOnDeletedBuyer() {
        UUID buyerId = UUID.randomUUID();
        Buyer buyer = new Buyer(buyerId, List.of(
                new BuyerCreated(buyerId, "Name"),
                new BuyerDeleted(buyerId)
        ));

        String expectedMessage = "Операция невозможна: покупатель удален.";

        IllegalStateException ex1 = assertThrows(IllegalStateException.class, () -> buyer.deposit(new BigDecimal("100")));
        assertEquals(expectedMessage, ex1.getMessage());

        IllegalStateException ex2 = assertThrows(IllegalStateException.class, () -> buyer.withdraw(new BigDecimal("100")));
        assertEquals(expectedMessage, ex2.getMessage());

        IllegalStateException ex3 = assertThrows(IllegalStateException.class, () -> buyer.pay(new BigDecimal("100")));
        assertEquals(expectedMessage, ex3.getMessage());

        IllegalStateException ex4 = assertThrows(IllegalStateException.class, () -> buyer.updateName("Name"));
        assertEquals(expectedMessage, ex4.getMessage());

        IllegalStateException ex5 = assertThrows(IllegalStateException.class, buyer::delete);
        assertEquals(expectedMessage, ex5.getMessage());
    }
}