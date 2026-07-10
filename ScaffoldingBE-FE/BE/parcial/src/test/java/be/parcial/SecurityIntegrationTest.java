package be.parcial;

import be.parcial.controllers.AuthController;
import be.parcial.dtos.AuthRequestDTO;
import be.parcial.dtos.RefreshTokenRequestDTO;
import be.parcial.dtos.RegisterRequestDTO;
import be.parcial.exceptions.GlobalExceptionHandler;
import be.parcial.security.JwtService;
import be.parcial.services.RefreshTokenService;
import be.parcial.services.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@SpringBootTest(classes = ParcialApplication.class)
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class SecurityIntegrationTest {

    private MockMvc mockMvc;

    @Autowired
    private org.springframework.security.authentication.AuthenticationManager authenticationManager;

    @Autowired
    private UserService userService;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private RefreshTokenService refreshTokenService;

    private JsonMapper jsonMapper;

    @BeforeEach
    void setUp() {
        jsonMapper = JsonMapper.builder().build();
        AuthController authController = new AuthController(
                authenticationManager, userService, jwtService, refreshTokenService);
        mockMvc = MockMvcBuilders.standaloneSetup(authController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("should authenticate with seeded user and get tokens")
    void authenticate_seededUser_returnsTokens() throws Exception {
        AuthRequestDTO request = new AuthRequestDTO("admin", "admin123");

        MockHttpServletResponse response = mockMvc.perform(post("/api/auth/authenticate")
                        .contentType("application/json")
                        .content(jsonMapper.writeValueAsString(request)))
                .andReturn().getResponse();

        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(response.getContentAsString()).contains("accessToken");
        assertThat(response.getContentAsString()).contains("refreshToken");
    }

    @Test
    @DisplayName("should return 401 with invalid credentials")
    void authenticate_invalidCredentials_returns401() throws Exception {
        AuthRequestDTO request = new AuthRequestDTO("admin", "wrongpassword");

        MockHttpServletResponse response = mockMvc.perform(post("/api/auth/authenticate")
                        .contentType("application/json")
                        .content(jsonMapper.writeValueAsString(request)))
                .andReturn().getResponse();

        assertThat(response.getStatus()).isEqualTo(401);
    }

    @Test
    @DisplayName("should refresh token with valid refresh token")
    void refresh_validToken_returns200() throws Exception {
        AuthRequestDTO authRequest = new AuthRequestDTO("admin", "admin123");
        MockHttpServletResponse authResponse = mockMvc.perform(post("/api/auth/authenticate")
                        .contentType("application/json")
                        .content(jsonMapper.writeValueAsString(authRequest)))
                .andReturn().getResponse();

        String refreshToken = extractRefreshToken(authResponse.getContentAsString());

        RefreshTokenRequestDTO refreshRequest = new RefreshTokenRequestDTO(refreshToken);
        MockHttpServletResponse refreshResponse = mockMvc.perform(post("/api/auth/refresh")
                        .contentType("application/json")
                        .content(jsonMapper.writeValueAsString(refreshRequest)))
                .andReturn().getResponse();

        assertThat(refreshResponse.getStatus()).isEqualTo(200);
        assertThat(refreshResponse.getContentAsString()).contains("accessToken");
        assertThat(refreshResponse.getContentAsString()).contains("refreshToken");
    }

    @Test
    @DisplayName("should logout and revoke token")
    void logout_validToken_returns200() throws Exception {
        AuthRequestDTO authRequest = new AuthRequestDTO("admin", "admin123");
        MockHttpServletResponse authResponse = mockMvc.perform(post("/api/auth/authenticate")
                        .contentType("application/json")
                        .content(jsonMapper.writeValueAsString(authRequest)))
                .andReturn().getResponse();

        String refreshToken = extractRefreshToken(authResponse.getContentAsString());

        RefreshTokenRequestDTO refreshRequest = new RefreshTokenRequestDTO(refreshToken);
        MockHttpServletResponse logoutResponse = mockMvc.perform(post("/api/auth/logout")
                        .contentType("application/json")
                        .content(jsonMapper.writeValueAsString(refreshRequest)))
                .andReturn().getResponse();

        assertThat(logoutResponse.getStatus()).isEqualTo(200);
    }

    private String extractRefreshToken(String jsonResponse) {
        try {
            JsonNode node = jsonMapper.readTree(jsonResponse);
            return node.get("refreshToken").asText();
        } catch (Exception e) {
            throw new RuntimeException("Failed to extract refresh token", e);
        }
    }
}
