package com.example.domain.model;

import com.example.domain.model.events.* ; // Импортируем все события
import com.example.domain.model.exceptions.InsufficientFundsException;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Buyer {

    private UUID id;
    private String name;
    private BigDecimal balance = BigDecimal.ZERO;
    private boolean isDeleted = false;

    private final List<DomainEvent> newEvents = new ArrayList<>();

    public Buyer(String name) {
        UUID id = UUID.randomUUID();
        applyChange(new BuyerCreated(id, name));
    }

    public Buyer(UUID id, List<DomainEvent> history) {
        this.id = id;
        history.forEach(this::apply);
    }

    public void updateName(String newName) {
        checkNotDeleted();
        if (!this.name.equals(newName)) {
            applyChange(new BuyerNameChanged(this.id, newName));
        }
    }

    public void delete() {
        checkNotDeleted();
        applyChange(new BuyerDeleted(this.id));
    }

    public void deposit(BigDecimal amount) {
        checkNotDeleted();
        applyChange(new MoneyDeposited(this.id, amount));
    }

    public void withdraw(BigDecimal amount) {
        checkNotDeleted();
        if (balance.compareTo(amount) < 0) throw new InsufficientFundsException(
                "У покупателя " + name + " недостаточно денег. Нужно: " + amount + ", есть: " + balance
        );

        applyChange(new MoneyWithdrawn(this.id, amount));
    }

    public void pay(BigDecimal amount) {
        withdraw(amount);
    }

    private void apply(DomainEvent e) {
        if (e instanceof BuyerCreated c) {
            this.id = c.buyerId();
            this.name = c.name();
        }
        else if (e instanceof MoneyDeposited d) {
            this.balance = this.balance.add(d.amount());
        }
        else if (e instanceof MoneyWithdrawn w) {
            this.balance = this.balance.subtract(w.amount());
        }
        else if (e instanceof BuyerNameChanged c) {
            this.name = c.newName();
        }
        else if (e instanceof BuyerDeleted d) {
            this.isDeleted = true;
        }
    }

    private void applyChange(DomainEvent e) {
        apply(e);
        newEvents.add(e);
    }

    private void checkNotDeleted() {
        if (isDeleted) {
            throw new IllegalStateException("Операция невозможна: покупатель удален.");
        }
    }

    public List<DomainEvent> getUncommitedChanges() {
        return newEvents;
    }

    public UUID getId() { return id; }
    public String getName() { return name; }
    public BigDecimal getBalance() { return balance; }
    public boolean isDeleted() { return isDeleted; }
}