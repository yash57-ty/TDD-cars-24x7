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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("Vehicle Search Integration Tests")
class VehicleSearchIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private VehicleRepository vehicleRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    private String userToken;

    @BeforeEach
    void setUp() {
        vehicleRepository.deleteAll();
        userRepository.deleteAll();

        // Create user and token
        User user = userRepository.save(User.builder()
                .name("Search User")
                .email("search@test.com")
                .password(passwordEncoder.encode("password"))
                .role(User.Role.USER)
                .build());

        var userDetails = org.springframework.security.core.userdetails.User.builder()
                .username(user.getEmail())
                .password(user.getPassword())
                .roles(user.getRole().name())
                .build();

        userToken = "Bearer " + jwtService.generateToken(userDetails);

        // Seed diverse test dataset
        vehicleRepository.save(Vehicle.builder()
                .make("Toyota").model("Camry").category("Sedan").price(new BigDecimal("25000.00")).quantity(5).build());

        vehicleRepository.save(Vehicle.builder()
                .make("Toyota").model("RAV4").category("SUV").price(new BigDecimal("30000.00")).quantity(3).build());

        vehicleRepository.save(Vehicle.builder()
                .make("Honda").model("Civic").category("Sedan").price(new BigDecimal("22000.00")).quantity(10).build());

        vehicleRepository.save(Vehicle.builder()
                .make("Ford").model("F-150").category("Truck").price(new BigDecimal("45000.00")).quantity(2).build());
    }

    @Test
    @DisplayName("GET /api/vehicles/search?make=Toyota - should return only Toyota vehicles")
    void search_ByMake_ShouldReturnFilteredList() throws Exception {
        mockMvc.perform(get("/api/vehicles/search")
                        .header("Authorization", userToken)
                        .param("make", "Toyota"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].make").value("Toyota"))
                .andExpect(jsonPath("$[1].make").value("Toyota"));
    }

    @Test
    @DisplayName("GET /api/vehicles/search?category=Sedan - should return only Sedan vehicles")
    void search_ByCategory_ShouldReturnFilteredList() throws Exception {
        mockMvc.perform(get("/api/vehicles/search")
                        .header("Authorization", userToken)
                        .param("category", "Sedan"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].category").value("Sedan"))
                .andExpect(jsonPath("$[1].category").value("Sedan"));
    }

    @Test
    @DisplayName("GET /api/vehicles/search?minPrice=23000&maxPrice=35000 - should return vehicles within price range")
    void search_ByPriceRange_ShouldReturnFilteredList() throws Exception {
        mockMvc.perform(get("/api/vehicles/search")
                        .header("Authorization", userToken)
                        .param("minPrice", "23000.00")
                        .param("maxPrice", "35000.00"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2)) // Camry (25k) and RAV4 (30k)
                .andExpect(jsonPath("$[0].model").value("Camry"))
                .andExpect(jsonPath("$[1].model").value("RAV4"));
    }

    @Test
    @DisplayName("GET /api/vehicles/search with no query parameters - should return all vehicles")
    void search_WithoutParams_ShouldReturnAll() throws Exception {
        mockMvc.perform(get("/api/vehicles/search")
                        .header("Authorization", userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(4));
    }
}
