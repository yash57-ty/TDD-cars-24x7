package org.example.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("InventoryController Integration Tests")
class InventoryControllerIntegrationTest {

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
    private Vehicle vehicleInStock;
    private Vehicle vehicleOutOfStock;

    @BeforeEach
    void setUp() {
        vehicleRepository.deleteAll();
        userRepository.deleteAll();

        // Create regular user and admin users
        User user = userRepository.save(User.builder()
                .name("Regular User").email("user@test.com").password(passwordEncoder.encode("password")).role(User.Role.USER).build());
        User admin = userRepository.save(User.builder()
                .name("Admin User").email("admin@test.com").password(passwordEncoder.encode("password")).role(User.Role.ADMIN).build());

        // Generate tokens
        var userDetails = org.springframework.security.core.userdetails.User.builder()
                .username(user.getEmail()).password(user.getPassword()).roles(user.getRole().name()).build();
        var adminDetails = org.springframework.security.core.userdetails.User.builder()
                .username(admin.getEmail()).password(admin.getPassword()).roles(admin.getRole().name()).build();

        userToken = "Bearer " + jwtService.generateToken(userDetails);
        adminToken = "Bearer " + jwtService.generateToken(adminDetails);

        // Seed vehicles
        vehicleInStock = vehicleRepository.save(Vehicle.builder()
                .make("Toyota").model("Camry").category("Sedan").price(new BigDecimal("25000.00")).quantity(5).build());

        vehicleOutOfStock = vehicleRepository.save(Vehicle.builder()
                .make("Honda").model("Civic").category("Sedan").price(new BigDecimal("20000.00")).quantity(0).build());
    }

    @Test
    @DisplayName("POST /api/vehicles/{id}/purchase - should decrement quantity and return 200")
    void purchase_ShouldReturn200_AndDecrementQuantity() throws Exception {
        mockMvc.perform(post("/api/vehicles/" + vehicleInStock.getId() + "/purchase")
                        .header("Authorization", userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.quantity").value(4));
    }

    @Test
    @DisplayName("POST /api/vehicles/{id}/purchase - should return 400 when out of stock")
    void purchase_ShouldReturn400_WhenOutOfStock() throws Exception {
        mockMvc.perform(post("/api/vehicles/" + vehicleOutOfStock.getId() + "/purchase")
                        .header("Authorization", userToken))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/vehicles/{id}/restock - Admin can restock and return 200")
    void restock_Admin_ShouldReturn200_AndIncrementQuantity() throws Exception {
        Map<String, Integer> body = Map.of("amount", 10);

        mockMvc.perform(post("/api/vehicles/" + vehicleInStock.getId() + "/restock")
                        .header("Authorization", adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.quantity").value(15));
    }

    @Test
    @DisplayName("POST /api/vehicles/{id}/restock - Regular user should return 403 Forbidden")
    void restock_User_ShouldReturn403() throws Exception {
        Map<String, Integer> body = Map.of("amount", 10);

        mockMvc.perform(post("/api/vehicles/" + vehicleInStock.getId() + "/restock")
                        .header("Authorization", userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("POST /api/vehicles/{id}/purchase - should return 401 when not authenticated")
    void purchase_ShouldReturn401_WhenNotAuthenticated() throws Exception {
        mockMvc.perform(post("/api/vehicles/" + vehicleInStock.getId() + "/purchase"))
                .andExpect(status().isUnauthorized());
    }
}
