package com.example.domain.model;

import com.example.domain.model.events.*;
import com.example.domain.model.exceptions.BusinessException;
import com.example.domain.model.exceptions.HouseAlreadySoldException;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class House {

    private UUID id;
    private String address;
    private BigDecimal price;
    private boolean isSold;
    private boolean isDeleted = false;

    private final List<DomainEvent> newEvents = new ArrayList<>();

    public House(String address, BigDecimal price) {
        UUID id = UUID.randomUUID();
        applyChange(new HouseCreated(id, address, price));
    }

    public House(UUID id, List<DomainEvent> history) {
        this.id = id;
        history.forEach(this::apply);
    }

    public void update(String address, BigDecimal price) {
        checkNotDeleted();
        if (isSold) {
            throw new BusinessException("Невозможно изменить данные проданного дома");
        }
        applyChange(new HouseUpdated(this.id, address, price));
    }

    public void delete() {
        checkNotDeleted();
        if (isSold) {
            throw new BusinessException("Невозможно удалить проданный дом из системы");
        }
        applyChange(new HouseDeleted(this.id));
    }

    public void sellTo(UUID buyerId) {
        checkNotDeleted();
        if (isSold) throw new HouseAlreadySoldException(
                "Дом по адресу " + address + " уже продан и не может быть куплен повторно."
        );
        applyChange(new HouseSold(this.id, buyerId));
    }

    private void apply(DomainEvent e) {
        if (e instanceof HouseCreated c) {
            this.id = c.houseId();
            this.address = c.address();
            this.price = c.price();
            this.isSold = false;
        }
        else if (e instanceof HouseSold) {
            this.isSold = true;
        }
        else if (e instanceof HouseUpdated u) {
            this.address = u.address();
            this.price = u.price();
        }
        else if (e instanceof HouseDeleted) {
            this.isDeleted = true;
        }
    }

    private void applyChange(DomainEvent e) {
        apply(e);
        newEvents.add(e);
    }

    private void checkNotDeleted() {
        if (isDeleted) {
            throw new BusinessException("Операция невозможна: дом был удален из системы.");
        }
    }

    public List<DomainEvent> getUncommitedChanges() {
        return newEvents;
    }

    public UUID getId() { return id; }
    public BigDecimal getPrice() { return price; }

    public boolean isSold() {
        return isSold;
    }
}