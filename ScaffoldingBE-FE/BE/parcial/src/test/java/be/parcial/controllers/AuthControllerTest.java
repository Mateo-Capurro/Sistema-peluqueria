package be.parcial.controllers;

import be.parcial.dtos.*;
import be.parcial.domain.entities.RefreshTokenEntity;
import be.parcial.domain.entities.UserEntity;
import be.parcial.exceptions.GlobalExceptionHandler;
import be.parcial.security.JwtService;
import be.parcial.services.RefreshTokenService;
import be.parcial.services.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import tools.jackson.databind.json.JsonMapper;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    private MockMvc mockMvc;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private UserService userService;

    @Mock
    private JwtService jwtService;

    @Mock
    private RefreshTokenService refreshTokenService;

    @InjectMocks
    private AuthController authController;

    private JsonMapper jsonMapper;

    private UserEntity userEntity;
    private RefreshTokenEntity refreshTokenEntity;
    private UserDetails userDetails;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(authController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        jsonMapper = JsonMapper.builder().build();

        userEntity = new UserEntity();
        userEntity.setId(1L);
        userEntity.setUsername("testuser");
        userEntity.setPassword("encodedPassword");
        userEntity.setName("Test User");
        userEntity.setRole(UserEntity.Role.CLIENTE);

        refreshTokenEntity = new RefreshTokenEntity();
        refreshTokenEntity.setId(1L);
        refreshTokenEntity.setToken("refresh-token-123");
        refreshTokenEntity.setUser(userEntity);
        refreshTokenEntity.setExpiryDate(LocalDateTime.now().plusDays(7));
        refreshTokenEntity.setRevoked(false);

        userDetails = new User(
                userEntity.getUsername(),
                userEntity.getPassword(),
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_CLIENTE")));
    }

    @Test
    @DisplayName("should register user and return tokens")
    void register_validRequest_returns201() throws Exception {
        when(userService.registerUser(any(RegisterRequestDTO.class), any())).thenReturn(userEntity);
        when(jwtService.generateAccessToken(any(UserDetails.class))).thenReturn("access-token-123");
        when(refreshTokenService.createRefreshToken(any(UserEntity.class))).thenReturn(refreshTokenEntity);

        MockHttpServletResponse response = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(
                                new RegisterRequestDTO("testuser", "password123", "Test User",
                                        "test@peluqueria.com", "1100000000", "40000000"))))
                .andReturn().getResponse();

        assertThat(response.getStatus()).isEqualTo(201);
        verify(userService).registerUser(any(RegisterRequestDTO.class), eq(UserEntity.Role.CLIENTE));
        verify(jwtService).generateAccessToken(any(UserDetails.class));
        verify(refreshTokenService).createRefreshToken(userEntity);
    }

    @Test
    @DisplayName("should authenticate user and return tokens")
    void authenticate_validCredentials_returns200() throws Exception {
        Authentication auth = mock(Authentication.class);
        when(auth.getPrincipal()).thenReturn(userDetails);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(auth);
        when(userService.findByUsername("testuser")).thenReturn(Optional.of(userEntity));
        when(jwtService.generateAccessToken(any(UserDetails.class))).thenReturn("access-token-123");
        when(refreshTokenService.createRefreshToken(any(UserEntity.class))).thenReturn(refreshTokenEntity);

        MockHttpServletResponse response = mockMvc.perform(post("/api/auth/authenticate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(
                                new AuthRequestDTO("testuser", "password123"))))
                .andReturn().getResponse();

        assertThat(response.getStatus()).isEqualTo(200);
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(userService).findByUsername("testuser");
        verify(jwtService).generateAccessToken(any(UserDetails.class));
        verify(refreshTokenService).createRefreshToken(userEntity);
    }

    @Test
    @DisplayName("should throw 400 when user not found after authentication")
    void authenticate_userNotFound_throws400() throws Exception {
        Authentication auth = mock(Authentication.class);
        when(auth.getPrincipal()).thenReturn(userDetails);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(auth);
        when(userService.findByUsername("testuser")).thenReturn(Optional.empty());

        MockHttpServletResponse response = mockMvc.perform(post("/api/auth/authenticate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(
                                new AuthRequestDTO("testuser", "password123"))))
                .andReturn().getResponse();

        assertThat(response.getStatus()).isEqualTo(400);
    }

    @Test
    @DisplayName("should refresh tokens")
    void refresh_validRefreshToken_returns200() throws Exception {
        when(refreshTokenService.validateRefreshToken("refresh-token-123"))
                .thenReturn(refreshTokenEntity);
        doNothing().when(refreshTokenService).revokeRefreshToken("refresh-token-123");
        when(refreshTokenService.createRefreshToken(userEntity)).thenReturn(refreshTokenEntity);
        when(jwtService.generateAccessToken(any(UserDetails.class))).thenReturn("new-access-token");

        MockHttpServletResponse response = mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(
                                new RefreshTokenRequestDTO("refresh-token-123"))))
                .andReturn().getResponse();

        assertThat(response.getStatus()).isEqualTo(200);
        verify(refreshTokenService).validateRefreshToken("refresh-token-123");
        verify(refreshTokenService).revokeRefreshToken("refresh-token-123");
        verify(refreshTokenService).createRefreshToken(userEntity);
    }

    @Test
    @DisplayName("should logout and revoke token")
    void logout_validRefreshToken_returns200() throws Exception {
        doNothing().when(refreshTokenService).revokeRefreshToken("refresh-token-123");

        MockHttpServletResponse response = mockMvc.perform(post("/api/auth/logout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(
                                new RefreshTokenRequestDTO("refresh-token-123"))))
                .andReturn().getResponse();

        assertThat(response.getStatus()).isEqualTo(200);
        verify(refreshTokenService).revokeRefreshToken("refresh-token-123");
    }
}
