package org.example.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.backend.dto.VehicleRequest;
import org.example.backend.model.User;
import org.example.backend.model.Vehicle;
import org.example.backend.repository.UserRepository;
import org.example.backend.repository.VehicleRepository;
import org.example.backend.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("VehicleController Integration Tests")
class VehicleControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private VehicleRepository vehicleRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    private String userToken;
    private String adminToken;
    private Vehicle savedVehicle;

    @BeforeEach
    void setUp() {
        vehicleRepository.deleteAll();
        userRepository.deleteAll();

        // Create a regular USER
        User user = userRepository.save(User.builder()
                .name("Regular User")
                .email("user@test.com")
                .password(passwordEncoder.encode("password"))
                .role(User.Role.USER)
                .build());

        // Create an ADMIN
        User admin = userRepository.save(User.builder()
                .name("Admin User")
                .email("admin@test.com")
                .password(passwordEncoder.encode("password"))
                .role(User.Role.ADMIN)
                .build());

        // Generate tokens
        var userDetails = org.springframework.security.core.userdetails.User.builder()
                .username(user.getEmail())
                .password(user.getPassword())
                .roles(user.getRole().name())
                .build();

        var adminDetails = org.springframework.security.core.userdetails.User.builder()
                .username(admin.getEmail())
                .password(admin.getPassword())
                .roles(admin.getRole().name())
                .build();

        userToken = "Bearer " + jwtService.generateToken(userDetails);
        adminToken = "Bearer " + jwtService.generateToken(adminDetails);

        // Pre-save a vehicle for details, update, and delete tests
        savedVehicle = vehicleRepository.save(Vehicle.builder()
                .make("Toyota")
                .model("Camry")
                .category("Sedan")
                .price(new BigDecimal("25000.00"))
                .quantity(10)
                .build());
    }

    @Test
    @DisplayName("POST /api/vehicles - should return 201 when authenticated")
    void addVehicle_ShouldReturn201() throws Exception {
        VehicleRequest request = new VehicleRequest();
        request.setMake("Honda");
        request.setModel("Accord");
        request.setCategory("Sedan");
        request.setPrice(new BigDecimal("28000.00"));
        request.setQuantity(5);

        mockMvc.perform(post("/api/vehicles")
                        .header("Authorization", userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.make").value("Honda"))
                .andExpect(jsonPath("$.id").isNumber());
    }

    @Test
    @DisplayName("POST /api/vehicles - should return 401 when not authenticated")
    void addVehicle_ShouldReturn401_WhenNotAuthenticated() throws Exception {
        VehicleRequest request = new VehicleRequest();
        request.setMake("Honda");
        request.setModel("Accord");
        request.setCategory("Sedan");
        request.setPrice(new BigDecimal("28000.00"));
        request.setQuantity(5);

        mockMvc.perform(post("/api/vehicles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("GET /api/vehicles - should return 200 and list of vehicles")
    void getAllVehicles_ShouldReturn200_WithList() throws Exception {
        mockMvc.perform(get("/api/vehicles")
                        .header("Authorization", userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].make").value("Toyota"));
    }

    @Test
    @DisplayName("GET /api/vehicles/{id} - should return vehicle details")
    void getVehicleById_ShouldReturn200() throws Exception {
        mockMvc.perform(get("/api/vehicles/" + savedVehicle.getId())
                        .header("Authorization", userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(savedVehicle.getId()))
                .andExpect(jsonPath("$.model").value("Camry"));
    }

    @Test
    @DisplayName("PUT /api/vehicles/{id} - should update vehicle and return 200")
    void updateVehicle_ShouldReturn200() throws Exception {
        VehicleRequest request = new VehicleRequest();
        request.setMake("Toyota");
        request.setModel("Camry Updated");
        request.setCategory("Sedan");
        request.setPrice(new BigDecimal("26000.00"));
        request.setQuantity(8);

        mockMvc.perform(put("/api/vehicles/" + savedVehicle.getId())
                        .header("Authorization", userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.model").value("Camry Updated"));
    }

    @Test
    @DisplayName("DELETE /api/vehicles/{id} - Admin can delete, returns 204")
    void deleteVehicle_Admin_ShouldReturn204() throws Exception {
        mockMvc.perform(delete("/api/vehicles/" + savedVehicle.getId())
                        .header("Authorization", adminToken))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("DELETE /api/vehicles/{id} - Regular user gets 403")
    void deleteVehicle_User_ShouldReturn403() throws Exception {
        mockMvc.perform(delete("/api/vehicles/" + savedVehicle.getId())
                        .header("Authorization", userToken))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("GET /api/vehicles - should return 200 without authentication (public browsing)")
    void getAllVehicles_ShouldReturn200_WithoutAuth() throws Exception {
        mockMvc.perform(get("/api/vehicles"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @DisplayName("POST /api/vehicles - should return 401 when not authenticated")
    void addVehicle_ShouldReturn401_WhenNoTokenProvided() throws Exception {
        VehicleRequest request = new VehicleRequest();
        request.setMake("Tesla");
        request.setModel("Model 3");
        request.setCategory("Electric");
        request.setPrice(new BigDecimal("45000.00"));
        request.setQuantity(3);

        mockMvc.perform(post("/api/vehicles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }
    @Test
    @DisplayName("GET /hello - should return 200 and 'Hello World' without authentication")
    void hello_ShouldReturnHelloWorld_WithoutAuth() throws Exception {
        mockMvc.perform(get("/hello"))
                .andExpect(status().isOk())
                .andExpect(content().string("Hello World"));
    }
}
