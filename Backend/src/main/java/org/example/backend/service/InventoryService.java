package org.example.backend.service;

import lombok.RequiredArgsConstructor;
import org.example.backend.dto.VehicleResponse;
import org.example.backend.exception.InsufficientStockException;
import org.example.backend.exception.ResourceNotFoundException;
import org.example.backend.model.Vehicle;
import org.example.backend.repository.VehicleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class InventoryService {

    private final VehicleRepository vehicleRepository;

    @Transactional
    public VehicleResponse purchase(Long vehicleId) {
        Vehicle vehicle = vehicleRepository.findByIdForUpdate(vehicleId)
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found with id: " + vehicleId));

        if (vehicle.getQuantity() <= 0) {
            throw new InsufficientStockException(
                "Vehicle '" + vehicle.getMake() + " " + vehicle.getModel() + "' is out of stock"
            );
        }

        vehicle.setQuantity(vehicle.getQuantity() - 1);
        Vehicle updatedVehicle = vehicleRepository.save(vehicle);
        return mapToResponse(updatedVehicle);
    }

    @Transactional
    public VehicleResponse restock(Long vehicleId, int amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Restock amount must be greater than 0");
        }

        Vehicle vehicle = vehicleRepository.findByIdForUpdate(vehicleId)
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found with id: " + vehicleId));

        vehicle.setQuantity(vehicle.getQuantity() + amount);
        Vehicle updatedVehicle = vehicleRepository.save(vehicle);
        return mapToResponse(updatedVehicle);
    }

    private VehicleResponse mapToResponse(Vehicle vehicle) {
        return VehicleResponse.builder()
                .id(vehicle.getId())
                .make(vehicle.getMake())
                .model(vehicle.getModel())
                .category(vehicle.getCategory())
                .price(vehicle.getPrice())
                .quantity(vehicle.getQuantity())
                .build();
    }
}
