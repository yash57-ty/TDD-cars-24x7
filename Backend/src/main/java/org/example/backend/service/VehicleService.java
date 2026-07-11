package org.example.backend.service;

import lombok.RequiredArgsConstructor;
import org.example.backend.dto.VehicleRequest;
import org.example.backend.dto.VehicleResponse;
import org.example.backend.exception.ResourceNotFoundException;
import org.example.backend.model.Vehicle;
import org.example.backend.repository.VehicleRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class VehicleService {

    private final VehicleRepository vehicleRepository;

    public VehicleResponse addVehicle(VehicleRequest request) {
        Vehicle vehicle = Vehicle.builder()
                .make(request.getMake())
                .model(request.getModel())
                .category(request.getCategory())
                .price(request.getPrice())
                .quantity(request.getQuantity())
                .build();

        Vehicle savedVehicle = vehicleRepository.save(vehicle);
        return mapToResponse(savedVehicle);
    }

    public List<VehicleResponse> getAllVehicles() {
        return vehicleRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public VehicleResponse getVehicleById(Long id) {
        Vehicle vehicle = vehicleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found with id: " + id));
        return mapToResponse(vehicle);
    }

    public VehicleResponse updateVehicle(Long id, VehicleRequest request) {
        Vehicle vehicle = vehicleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found with id: " + id));

        vehicle.setMake(request.getMake());
        vehicle.setModel(request.getModel());
        vehicle.setCategory(request.getCategory());
        vehicle.setPrice(request.getPrice());
        vehicle.setQuantity(request.getQuantity());

        Vehicle updatedVehicle = vehicleRepository.save(vehicle);
        return mapToResponse(updatedVehicle);
    }

    public void deleteVehicle(Long id) {
        if (!vehicleRepository.existsById(id)) {
            throw new ResourceNotFoundException("Vehicle not found with id: " + id);
        }
        vehicleRepository.deleteById(id);
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
