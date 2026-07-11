package org.example.backend.service;

import org.example.backend.dto.VehicleResponse;
import org.example.backend.exception.InsufficientStockException;
import org.example.backend.exception.ResourceNotFoundException;
import org.example.backend.model.Vehicle;
import org.example.backend.repository.VehicleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("InventoryService Unit Tests")
class InventoryServiceTest {

    @Mock
    private VehicleRepository vehicleRepository;

    @InjectMocks
    private InventoryService inventoryService;

    private Vehicle vehicleInStock;
    private Vehicle vehicleOutOfStock;

    @BeforeEach
    void setUp() {
        vehicleInStock = Vehicle.builder()
                .id(1L).make("Toyota").model("Camry").category("Sedan")
                .price(new BigDecimal("25000.00")).quantity(5)
                .build();

        vehicleOutOfStock = Vehicle.builder()
                .id(2L).make("Honda").model("Civic").category("Sedan")
                .price(new BigDecimal("20000.00")).quantity(0)
                .build();
    }

    // ============================================================
    // TEST 1: purchase() — happy path, quantity decrements
    // ============================================================
    @Test
    @DisplayName("purchase() - should decrement quantity by 1")
    void purchase_ShouldDecrementQuantity() {
        // GIVEN — Service now uses findByIdForUpdate (pessimistic lock)
        when(vehicleRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(vehicleInStock));
        when(vehicleRepository.save(any(Vehicle.class))).thenAnswer(inv -> inv.getArgument(0));

        // WHEN
        VehicleResponse response = inventoryService.purchase(1L);

        // THEN
        assertThat(response.getQuantity()).isEqualTo(4);
        verify(vehicleRepository).save(argThat(v -> v.getQuantity() == 4));
    }

    // ============================================================
    // TEST 2: purchase() — out of stock throws InsufficientStockException
    // ============================================================
    @Test
    @DisplayName("purchase() - should throw InsufficientStockException when quantity is 0")
    void purchase_ShouldThrow_WhenOutOfStock() {
        // GIVEN
        when(vehicleRepository.findByIdForUpdate(2L)).thenReturn(Optional.of(vehicleOutOfStock));

        // WHEN / THEN
        assertThatThrownBy(() -> inventoryService.purchase(2L))
                .isInstanceOf(InsufficientStockException.class)
                .hasMessageContaining("out of stock");

        verify(vehicleRepository, never()).save(any());
    }

    // ============================================================
    // TEST 3: purchase() — vehicle not found throws ResourceNotFoundException
    // ============================================================
    @Test
    @DisplayName("purchase() - should throw ResourceNotFoundException when vehicle not found")
    void purchase_ShouldThrow_WhenVehicleNotFound() {
        // GIVEN — mock returns empty for unknown id
        when(vehicleRepository.findByIdForUpdate(999L)).thenReturn(Optional.empty());

        // WHEN / THEN
        assertThatThrownBy(() -> inventoryService.purchase(999L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ============================================================
    // TEST 4: restock() — happy path, quantity increments
    // ============================================================
    @Test
    @DisplayName("restock() - should increment quantity by given amount")
    void restock_ShouldIncrementQuantity() {
        // GIVEN
        when(vehicleRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(vehicleInStock));
        when(vehicleRepository.save(any(Vehicle.class))).thenAnswer(inv -> inv.getArgument(0));

        // WHEN
        VehicleResponse response = inventoryService.restock(1L, 10);

        // THEN
        assertThat(response.getQuantity()).isEqualTo(15);
        verify(vehicleRepository).save(argThat(v -> v.getQuantity() == 15));
    }

    // ============================================================
    // TEST 5: restock() — invalid amount throws IllegalArgumentException
    // ============================================================
    @Test
    @DisplayName("restock() - should throw when amount is 0 or negative")
    void restock_ShouldThrow_WhenAmountIsInvalid() {
        // WHEN / THEN — no repository call needed for invalid amount guard
        assertThatThrownBy(() -> inventoryService.restock(1L, 0))
                .isInstanceOf(IllegalArgumentException.class);

        assertThatThrownBy(() -> inventoryService.restock(1L, -5))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
