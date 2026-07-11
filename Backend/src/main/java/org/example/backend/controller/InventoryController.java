package org.example.backend.controller;

import lombok.RequiredArgsConstructor;
import org.example.backend.dto.VehicleResponse;
import org.example.backend.service.InventoryService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/vehicles")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService inventoryService;

    @PostMapping("/{id}/purchase")
    public ResponseEntity<VehicleResponse> purchase(@PathVariable Long id) {
        VehicleResponse response = inventoryService.purchase(id);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/restock")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<VehicleResponse> restock(
            @PathVariable Long id,
            @RequestBody Map<String, Integer> body
    ) {
        int amount = body.getOrDefault("amount", 1);
        VehicleResponse response = inventoryService.restock(id, amount);
        return ResponseEntity.ok(response);
    }
}
