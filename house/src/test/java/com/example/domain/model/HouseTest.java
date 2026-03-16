package com.example.domain.model;

import com.example.domain.model.events.HouseCreated;
import com.example.domain.model.events.HouseDeleted;
import com.example.domain.model.events.HouseSold;
import com.example.domain.model.events.HouseUpdated;
import com.example.domain.model.exceptions.BusinessException;
import com.example.domain.model.exceptions.HouseAlreadySoldException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class HouseTest {

    @Test
    void shouldCreateHouseAndGenerateEvent() {
        House house = new House("Address", new BigDecimal("1000"));

        assertNotNull(house.getId());
        assertEquals(new BigDecimal("1000"), house.getPrice());
        assertFalse(house.isSold());

        assertEquals(1, house.getUncommitedChanges().size());
        assertInstanceOf(HouseCreated.class, house.getUncommitedChanges().getFirst());
    }

    @Test
    void shouldRestoreHouseFromHistoryAndSell() {
        UUID houseId = UUID.randomUUID();
        HouseCreated createdEvent = new HouseCreated(houseId, "Address", new BigDecimal("500"));
        House house = new House(houseId, List.of(createdEvent));

        UUID buyerId = UUID.randomUUID();
        house.sellTo(buyerId);

        assertTrue(house.isSold());
        assertEquals(1, house.getUncommitedChanges().size());
        assertInstanceOf(HouseSold.class, house.getUncommitedChanges().getFirst());
    }

    @Test
    void shouldUpdateHouseSuccessfully() {
        UUID houseId = UUID.randomUUID();
        House house = new House(houseId, List.of(new HouseCreated(houseId, "Old Address", new BigDecimal("500"))));

        house.update("New Address", new BigDecimal("1000"));

        assertEquals(1, house.getUncommitedChanges().size());
        HouseUpdated event = (HouseUpdated) house.getUncommitedChanges().getFirst();
        assertEquals("New Address", event.address());
        assertEquals(new BigDecimal("1000"), event.price());
    }

    @Test
    void shouldDeleteHouseSuccessfully() {
        UUID houseId = UUID.randomUUID();
        House house = new House(houseId, List.of(new HouseCreated(houseId, "Address", new BigDecimal("500"))));

        house.delete();

        assertEquals(1, house.getUncommitedChanges().size());
        assertInstanceOf(HouseDeleted.class, house.getUncommitedChanges().getFirst());
    }

    @Test
    void shouldThrowExceptionWhenSellingAlreadySoldHouse() {
        UUID houseId = UUID.randomUUID();
        House house = new House(houseId, List.of(
                new HouseCreated(houseId, "Address", new BigDecimal("500")),
                new HouseSold(houseId, UUID.randomUUID())
        ));

        assertThrows(HouseAlreadySoldException.class, () -> house.sellTo(UUID.randomUUID()));
    }

    @Test
    void shouldThrowExceptionWhenUpdatingSoldHouse() {
        UUID houseId = UUID.randomUUID();
        House house = new House(houseId, List.of(
                new HouseCreated(houseId, "Address", new BigDecimal("500")),
                new HouseSold(houseId, UUID.randomUUID())
        ));

        BusinessException exception = assertThrows(BusinessException.class,
                () -> house.update("New Addr", new BigDecimal("1000")));

        assertEquals("Невозможно изменить данные проданного дома", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenDeletingSoldHouse() {
        UUID houseId = UUID.randomUUID();
        House house = new House(houseId, List.of(
                new HouseCreated(houseId, "Address", new BigDecimal("500")),
                new HouseSold(houseId, UUID.randomUUID())
        ));

        BusinessException exception = assertThrows(BusinessException.class, house::delete);
        assertEquals("Невозможно удалить проданный дом из системы", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenOperatingOnDeletedHouse() {
        UUID houseId = UUID.randomUUID();
        House house = new House(houseId, List.of(
                new HouseCreated(houseId, "Address", new BigDecimal("500")),
                new HouseDeleted(houseId)
        ));

        String expectedMessage = "Операция невозможна: дом был удален из системы.";

        BusinessException sellEx = assertThrows(BusinessException.class, () -> house.sellTo(UUID.randomUUID()));
        assertEquals(expectedMessage, sellEx.getMessage());

        BusinessException updateEx = assertThrows(BusinessException.class, () -> house.update("A", new BigDecimal("1")));
        assertEquals(expectedMessage, updateEx.getMessage());

        BusinessException deleteEx = assertThrows(BusinessException.class, house::delete);
        assertEquals(expectedMessage, deleteEx.getMessage());
    }
}