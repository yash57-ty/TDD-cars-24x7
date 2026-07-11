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

    @Test
    @DisplayName("purchase() - should decrement quantity by 1")
    void purchase_ShouldDecrementQuantity() {
        when(vehicleRepository.findById(1L)).thenReturn(Optional.of(vehicleInStock));
        when(vehicleRepository.save(any(Vehicle.class))).thenAnswer(inv -> inv.getArgument(0));

        VehicleResponse response = inventoryService.purchase(1L);

        assertThat(response.getQuantity()).isEqualTo(4);
        verify(vehicleRepository).save(argThat(v -> v.getQuantity() == 4));
    }

    @Test
    @DisplayName("purchase() - should throw InsufficientStockException when quantity is 0")
    void purchase_ShouldThrow_WhenOutOfStock() {
        when(vehicleRepository.findById(2L)).thenReturn(Optional.of(vehicleOutOfStock));

        assertThatThrownBy(() -> inventoryService.purchase(2L))
                .isInstanceOf(InsufficientStockException.class)
                .hasMessageContaining("out of stock");

        verify(vehicleRepository, never()).save(any());
    }

    @Test
    @DisplayName("purchase() - should throw ResourceNotFoundException when vehicle not found")
    void purchase_ShouldThrow_WhenVehicleNotFound() {
        when(vehicleRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> inventoryService.purchase(999L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("restock() - should increment quantity by given amount")
    void restock_ShouldIncrementQuantity() {
        when(vehicleRepository.findById(1L)).thenReturn(Optional.of(vehicleInStock));
        when(vehicleRepository.save(any(Vehicle.class))).thenAnswer(inv -> inv.getArgument(0));

        VehicleResponse response = inventoryService.restock(1L, 10);

        assertThat(response.getQuantity()).isEqualTo(15);
        verify(vehicleRepository).save(argThat(v -> v.getQuantity() == 15));
    }

    @Test
    @DisplayName("restock() - should throw when amount is 0 or negative")
    void restock_ShouldThrow_WhenAmountIsInvalid() {
        assertThatThrownBy(() -> inventoryService.restock(1L, 0))
                .isInstanceOf(IllegalArgumentException.class);

        assertThatThrownBy(() -> inventoryService.restock(1L, -5))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
