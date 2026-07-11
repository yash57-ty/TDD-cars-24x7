package org.example.backend.security;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.security.SignatureException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("JwtService Security & Signature Validation Tests")
class JwtServiceTest {

    private JwtService jwtService;
    private UserDetails userDetails;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        
        // Inject properties matching application-test.properties configuration
        ReflectionTestUtils.setField(jwtService, "secretKey", "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970");
        ReflectionTestUtils.setField(jwtService, "jwtExpiration", 3600000L); // 1 hour

        userDetails = new User("security@test.com", "password", new ArrayList<>());
    }

    @Test
    @DisplayName("generateToken() - should create a verifiable JWT token")
    void generateToken_ShouldCreateValidToken() {
        String token = jwtService.generateToken(userDetails);
        
        assertThat(token).isNotEmpty();
        assertThat(jwtService.extractUsername(token)).isEqualTo("security@test.com");
        assertThat(jwtService.isTokenValid(token, userDetails)).isTrue();
    }

    @Test
    @DisplayName("isTokenValid() - should reject token for wrong user details")
    void isTokenValid_ShouldReject_ForWrongUser() {
        String token = jwtService.generateToken(userDetails);
        UserDetails wrongUser = new User("wrong@test.com", "password", new ArrayList<>());
        
        assertThat(jwtService.isTokenValid(token, wrongUser)).isFalse();
    }

    @Test
    @DisplayName("isTokenValid() - should reject expired tokens")
    void isTokenValid_ShouldReject_ExpiredTokens() {
        // Set short expiration (1 millisecond) to simulate expiration
        ReflectionTestUtils.setField(jwtService, "jwtExpiration", 1L);
        String token = jwtService.generateToken(userDetails);

        // Sleep briefly to ensure it expires
        try {
            Thread.sleep(5);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        assertThatThrownBy(() -> jwtService.isTokenValid(token, userDetails))
                .isInstanceOf(ExpiredJwtException.class);
    }

    @Test
    @DisplayName("extractUsername() - should fail when signature is invalid (different key)")
    void extractUsername_ShouldFail_WhenSignatureIsInvalid() {
        String token = jwtService.generateToken(userDetails);

        // Create a secondary JwtService with a different secret key
        JwtService malignedJwtService = new JwtService();
        ReflectionTestUtils.setField(malignedJwtService, "secretKey", "7970404E635266556A586E3272357538782F413F4428472B4B6250645367566B");
        ReflectionTestUtils.setField(malignedJwtService, "jwtExpiration", 3600000L);

        assertThatThrownBy(() -> malignedJwtService.extractUsername(token))
                .isInstanceOf(SignatureException.class);
    }
}
