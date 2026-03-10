package com.example.infrastructure.web;

import com.example.application.ports.HouseQueryService;
import com.example.application.sagas.AsyncBuyHouseSaga;
import com.example.application.sagas.BuyHouseSaga;
import com.example.application.usecases.CreateHouseUseCase;
import com.example.application.usecases.DeleteHouseUseCase;
import com.example.application.usecases.UpdateHouseUseCase;
import com.example.infrastructure.web.dto.request.BuyHouseRequest;
import com.example.infrastructure.web.dto.request.CreateHouseRequest;
import com.example.infrastructure.web.dto.request.UpdateHouseRequest;
import com.example.infrastructure.web.dto.response.HouseResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/houses")
@RequiredArgsConstructor
public class HousesController {

    private final CreateHouseUseCase createHouseUseCase;
    private final UpdateHouseUseCase updateHouseUseCase;
    private final DeleteHouseUseCase deleteHouseUseCase;
    private final BuyHouseSaga buyHouseSaga;
    private final AsyncBuyHouseSaga asyncBuyHouseSaga;

    private final HouseQueryService houseQueryService;

    @PostMapping
    public ResponseEntity<UUID> createHouse(@RequestBody CreateHouseRequest dto) {
        UUID id = createHouseUseCase.execute(dto.address(), dto.price());
        return ResponseEntity.created(URI.create("/houses/" + id)).body(id);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Void> updateHouse(@PathVariable UUID id, @RequestBody UpdateHouseRequest dto) {
        updateHouseUseCase.execute(id, dto.address(), dto.price());
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteHouse(@PathVariable UUID id) {
        deleteHouseUseCase.execute(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/buy")
    public ResponseEntity<Void> buyHouse(@PathVariable UUID id, @RequestBody BuyHouseRequest dto) {
        //buyHouseSaga.execute(id, dto.buyerId());
        asyncBuyHouseSaga.execute(id, dto.buyerId());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<HouseResponse> getHouse(@PathVariable UUID id) {
        return houseQueryService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    public ResponseEntity<List<HouseResponse>> getAllHouses() {
        List<HouseResponse> houses = houseQueryService.findAll();
        return ResponseEntity.ok(houses);
    }
}