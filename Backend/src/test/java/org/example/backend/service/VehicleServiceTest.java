package org.example.backend.service;

import org.example.backend.dto.VehicleRequest;
import org.example.backend.dto.VehicleResponse;
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
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("VehicleService Unit Tests")
class VehicleServiceTest {

    @Mock
    private VehicleRepository vehicleRepository;

    @InjectMocks
    private VehicleService vehicleService;

    private Vehicle vehicle;
    private VehicleRequest vehicleRequest;

    @BeforeEach
    void setUp() {
        vehicle = Vehicle.builder()
                .id(1L)
                .make("Toyota")
                .model("Camry")
                .category("Sedan")
                .price(new BigDecimal("25000.00"))
                .quantity(10)
                .build();

        vehicleRequest = new VehicleRequest();
        vehicleRequest.setMake("Toyota");
        vehicleRequest.setModel("Camry");
        vehicleRequest.setCategory("Sedan");
        vehicleRequest.setPrice(new BigDecimal("25000.00"));
        vehicleRequest.setQuantity(10);
    }

    @Test
    @DisplayName("addVehicle() - should save and return VehicleResponse")
    void addVehicle_ShouldSaveAndReturnResponse() {
        when(vehicleRepository.save(any(Vehicle.class))).thenReturn(vehicle);

        VehicleResponse response = vehicleService.addVehicle(vehicleRequest);

        assertThat(response).isNotNull();
        assertThat(response.getMake()).isEqualTo("Toyota");
        assertThat(response.getModel()).isEqualTo("Camry");
        assertThat(response.getPrice()).isEqualByComparingTo("25000.00");
        assertThat(response.getQuantity()).isEqualTo(10);
        verify(vehicleRepository, times(1)).save(any(Vehicle.class));
    }

    @Test
    @DisplayName("getAllVehicles() - should return list of all vehicles")
    void getAllVehicles_ShouldReturnList() {
        Vehicle vehicle2 = Vehicle.builder()
                .id(2L).make("Honda").model("Civic").category("Sedan")
                .price(new BigDecimal("20000.00")).quantity(5).build();

        when(vehicleRepository.findAll()).thenReturn(List.of(vehicle, vehicle2));

        List<VehicleResponse> result = vehicleService.getAllVehicles();

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getMake()).isEqualTo("Toyota");
        assertThat(result.get(1).getMake()).isEqualTo("Honda");
    }

    @Test
    @DisplayName("getVehicleById() - should return vehicle when found")
    void getVehicleById_ShouldReturnVehicle_WhenFound() {
        when(vehicleRepository.findById(1L)).thenReturn(Optional.of(vehicle));

        VehicleResponse response = vehicleService.getVehicleById(1L);

        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getMake()).isEqualTo("Toyota");
    }

    @Test
    @DisplayName("getVehicleById() - should throw ResourceNotFoundException when not found")
    void getVehicleById_ShouldThrow_WhenNotFound() {
        when(vehicleRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> vehicleService.getVehicleById(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("999");
    }

    @Test
    @DisplayName("updateVehicle() - should update and return updated vehicle")
    void updateVehicle_ShouldUpdateAndReturn() {
        VehicleRequest updateRequest = new VehicleRequest();
        updateRequest.setMake("Toyota");
        updateRequest.setModel("Corolla");
        updateRequest.setCategory("Sedan");
        updateRequest.setPrice(new BigDecimal("22000.00"));
        updateRequest.setQuantity(8);

        Vehicle updatedVehicle = Vehicle.builder()
                .id(1L).make("Toyota").model("Corolla").category("Sedan")
                .price(new BigDecimal("22000.00")).quantity(8).build();

        when(vehicleRepository.findById(1L)).thenReturn(Optional.of(vehicle));
        when(vehicleRepository.save(any(Vehicle.class))).thenReturn(updatedVehicle);

        VehicleResponse response = vehicleService.updateVehicle(1L, updateRequest);

        assertThat(response.getModel()).isEqualTo("Corolla");
        assertThat(response.getPrice()).isEqualByComparingTo("22000.00");
    }

    @Test
    @DisplayName("deleteVehicle() - should delete vehicle when found")
    void deleteVehicle_ShouldDelete_WhenFound() {
        when(vehicleRepository.existsById(1L)).thenReturn(true);

        vehicleService.deleteVehicle(1L);

        verify(vehicleRepository, times(1)).deleteById(1L);
    }

    @Test
    @DisplayName("deleteVehicle() - should throw when vehicle not found")
    void deleteVehicle_ShouldThrow_WhenNotFound() {
        when(vehicleRepository.existsById(999L)).thenReturn(false);

        assertThatThrownBy(() -> vehicleService.deleteVehicle(999L))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(vehicleRepository, never()).deleteById(any());
    }
}
