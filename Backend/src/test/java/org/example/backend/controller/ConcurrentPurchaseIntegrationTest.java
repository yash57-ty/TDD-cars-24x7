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
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("E2E Concurrent Purchase Scalability Tests")
class ConcurrentPurchaseIntegrationTest {

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
    private Vehicle limitedVehicle;

    @BeforeEach
    void setUp() {
        vehicleRepository.deleteAll();
        userRepository.deleteAll();

        // Create user and token
        User user = userRepository.save(User.builder()
                .name("Load User")
                .email("load@test.com")
                .password(passwordEncoder.encode("password"))
                .role(User.Role.USER)
                .build());

        var userDetails = org.springframework.security.core.userdetails.User.builder()
                .username(user.getEmail())
                .password(user.getPassword())
                .roles(user.getRole().name())
                .build();

        userToken = "Bearer " + jwtService.generateToken(userDetails);

        // Seed vehicle with exactly 3 items in stock
        limitedVehicle = vehicleRepository.save(Vehicle.builder()
                .make("Toyota")
                .model("Supra")
                .category("Sports")
                .price(new BigDecimal("55000.00"))
                .quantity(3)
                .build());
    }

    @Test
    @DisplayName("Simulate 10 concurrent purchases - exactly 3 should succeed, 7 should fail, stock must end at 0")
    void simulateConcurrentPurchases() throws Exception {
        int numberOfThreads = 10;
        ExecutorService executorService = Executors.newFixedThreadPool(numberOfThreads);
        CountDownLatch latch = new CountDownLatch(1);
        CountDownLatch finishLatch = new CountDownLatch(numberOfThreads);

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failureCount = new AtomicInteger(0);

        for (int i = 0; i < numberOfThreads; i++) {
            executorService.submit(() -> {
                try {
                    latch.await(); // wait until release signal
                    MvcResult result = mockMvc.perform(post("/api/vehicles/" + limitedVehicle.getId() + "/purchase")
                                    .header("Authorization", userToken))
                            .andReturn();

                    int status = result.getResponse().getStatus();
                    if (status == 200) {
                        successCount.incrementAndGet();
                    } else if (status == 400) {
                        failureCount.incrementAndGet();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    finishLatch.countDown();
                }
            });
        }

        latch.countDown(); // release latch to start threads simultaneously
        finishLatch.await(); // wait for all threads to finish

        // Verify database state
        Vehicle updatedVehicle = vehicleRepository.findById(limitedVehicle.getId()).orElseThrow();

        assertThat(successCount.get()).isEqualTo(3);
        assertThat(failureCount.get()).isEqualTo(7);
        assertThat(updatedVehicle.getQuantity()).isEqualTo(0);

        executorService.shutdown();
    }
}
