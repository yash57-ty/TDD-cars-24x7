package org.example.backend.service;

import org.example.backend.dto.AuthResponse;
import org.example.backend.dto.LoginRequest;
import org.example.backend.dto.RegisterRequest;
import org.example.backend.exception.EmailAlreadyExistsException;
import org.example.backend.model.User;
import org.example.backend.repository.UserRepository;
import org.example.backend.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService Unit Tests")
class AuthServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtService jwtService;
    @Mock private AuthenticationManager authenticationManager;

    @InjectMocks
    private AuthService authService;

    private RegisterRequest registerRequest;
    private LoginRequest loginRequest;
    private User savedUser;

    @BeforeEach
    void setUp() {
        registerRequest = new RegisterRequest();
        registerRequest.setName("John Doe");
        registerRequest.setEmail("john@example.com");
        registerRequest.setPassword("password123");

        loginRequest = new LoginRequest();
        loginRequest.setEmail("john@example.com");
        loginRequest.setPassword("password123");

        savedUser = User.builder()
                .id(1L)
                .name("John Doe")
                .email("john@example.com")
                .password("encodedPassword")
                .role(User.Role.USER)
                .build();
    }

    // ============================================================
    // TEST 1: register() — happy path
    // ============================================================
    @Test
    @DisplayName("register() - should save user and return JWT token")
    void register_ShouldSaveUserAndReturnToken() {
        // GIVEN
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        when(jwtService.generateToken(any(UserDetails.class))).thenReturn("mocked.jwt.token");

        // WHEN
        AuthResponse response = authService.register(registerRequest);

        // THEN
        assertThat(response).isNotNull();
        assertThat(response.getToken()).isEqualTo("mocked.jwt.token");
        assertThat(response.getEmail()).isEqualTo("john@example.com");
        assertThat(response.getRole()).isEqualTo(User.Role.USER);
        verify(userRepository, times(1)).save(any(User.class));
        verify(passwordEncoder, times(1)).encode("password123");
    }

    // ============================================================
    // TEST 2: register() — duplicate email
    // ============================================================
    @Test
    @DisplayName("register() - should throw EmailAlreadyExistsException when email is taken")
    void register_ShouldThrow_WhenEmailAlreadyExists() {
        // GIVEN
        when(userRepository.existsByEmail("john@example.com")).thenReturn(true);

        // WHEN / THEN
        assertThatThrownBy(() -> authService.register(registerRequest))
                .isInstanceOf(EmailAlreadyExistsException.class)
                .hasMessageContaining("john@example.com");

        verify(userRepository, never()).save(any());
    }

    // ============================================================
    // TEST 3: register() — password must be encoded
    // ============================================================
    @Test
    @DisplayName("register() - should BCrypt encode password before saving")
    void register_ShouldEncodePassword_BeforeSaving() {
        // GIVEN
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("$2a$10$hashedValue");
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        when(jwtService.generateToken(any(UserDetails.class))).thenReturn("token");

        // WHEN
        authService.register(registerRequest);

        // THEN — password saved must be the encoded one
        verify(userRepository).save(argThat(user ->
                user.getPassword().equals("$2a$10$hashedValue")
        ));
    }

    // ============================================================
    // TEST 4: login() — happy path
    // ============================================================
    @Test
    @DisplayName("login() - should return JWT token on valid credentials")
    void login_ShouldReturnToken_WhenCredentialsValid() {
        // GIVEN
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(null);
        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(savedUser));
        when(jwtService.generateToken(any(UserDetails.class))).thenReturn("login.jwt.token");

        // WHEN
        AuthResponse response = authService.login(loginRequest);

        // THEN
        assertThat(response.getToken()).isEqualTo("login.jwt.token");
        assertThat(response.getEmail()).isEqualTo("john@example.com");
        verify(authenticationManager).authenticate(any());
    }
}
