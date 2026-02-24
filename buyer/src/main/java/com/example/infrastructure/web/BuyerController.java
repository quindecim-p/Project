package com.example.infrastructure.web;

import com.example.application.ports.BuyerQueryService;
import com.example.application.usecases.CreateBuyerUseCase;
import com.example.application.usecases.DeleteBuyerUseCase;
import com.example.application.usecases.DepositMoneyUseCase;
import com.example.application.usecases.UpdateBuyerUseCase;
import com.example.infrastructure.web.dto.request.CreateBuyerRequest;
import com.example.infrastructure.web.dto.request.DepositRequest;
import com.example.infrastructure.web.dto.request.UpdateBuyerRequest;
import com.example.infrastructure.web.dto.response.BuyerResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/buyers")
@RequiredArgsConstructor
public class BuyerController {

    private final CreateBuyerUseCase createBuyerUseCase;
    private final UpdateBuyerUseCase updateBuyerUseCase;
    private final DeleteBuyerUseCase deleteBuyerUseCase;
    private final DepositMoneyUseCase depositMoneyUseCase;
    private final BuyerQueryService buyerQueryService;

    @PostMapping
    public ResponseEntity<UUID> createBuyer(@RequestBody CreateBuyerRequest dto) {
        UUID id = createBuyerUseCase.execute(dto.name());
        return ResponseEntity.created(URI.create("/buyers/" + id)).body(id);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Void> updateBuyer(@PathVariable UUID id, @RequestBody UpdateBuyerRequest dto) {
        updateBuyerUseCase.execute(id, dto.name());
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBuyer(@PathVariable UUID id) {
        deleteBuyerUseCase.execute(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/deposit")
    public ResponseEntity<Void> deposit(@PathVariable UUID id, @RequestBody DepositRequest dto) {
        depositMoneyUseCase.execute(id, dto.amount());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<BuyerResponse> getBuyer(@PathVariable UUID id) {
        return buyerQueryService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    public ResponseEntity<List<BuyerResponse>> getAllBuyers() {
        List<BuyerResponse> buyers = buyerQueryService.findAll();
        return ResponseEntity.ok(buyers);
    }
}