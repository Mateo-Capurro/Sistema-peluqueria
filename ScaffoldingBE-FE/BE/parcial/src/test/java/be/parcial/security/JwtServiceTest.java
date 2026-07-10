package be.parcial.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collections;
import java.util.Date;
import java.util.HashMap;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class JwtServiceTest {

    @InjectMocks
    private JwtService jwtService;

    private UserDetails userDetails;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(jwtService, "secretKey",
                "dGVzdC1zZWNyZXQta2V5LWZvci1kZXZlbG9wbWVudC1vbmx5LW1pbmltdW0tMzItYml0cw==");
        ReflectionTestUtils.setField(jwtService, "accessTokenExpiration", 900000L);
        ReflectionTestUtils.setField(jwtService, "refreshTokenExpiration", 604800000L);
        ReflectionTestUtils.setField(jwtService, "issuer", "parcial-app");

        userDetails = new User("testuser", "password",
                Collections.emptyList());
    }

    @Test
    @DisplayName("should generate valid access token")
    void generateAccessToken_validUser_returnsToken() {
        String token = jwtService.generateAccessToken(userDetails);

        assertThat(token).isNotNull();
        assertThat(token).isNotBlank();
    }

    @Test
    @DisplayName("should generate valid refresh token")
    void generateRefreshToken_validUser_returnsToken() {
        String token = jwtService.generateRefreshToken(userDetails);

        assertThat(token).isNotNull();
        assertThat(token).isNotBlank();
    }

    @Test
    @DisplayName("should extract username from token")
    void extractUsername_validToken_returnsUsername() {
        String token = jwtService.generateAccessToken(userDetails);

        String username = jwtService.extractUsername(token);

        assertThat(username).isEqualTo("testuser");
    }

    @Test
    @DisplayName("should extract expiration from token")
    void extractExpiration_validToken_returnsDate() {
        String token = jwtService.generateAccessToken(userDetails);

        Date expiration = jwtService.extractExpiration(token);

        assertThat(expiration).isNotNull();
        assertThat(expiration).isAfter(new Date());
    }

    @Test
    @DisplayName("should validate token correctly")
    void isTokenValid_validToken_returnsTrue() {
        String token = jwtService.generateAccessToken(userDetails);

        boolean valid = jwtService.isTokenValid(token, userDetails);

        assertThat(valid).isTrue();
    }

    @Test
    @DisplayName("should return false for invalid token")
    void isTokenValid_invalidToken_returnsFalse() {
        boolean valid = jwtService.isTokenValid("invalid.token.here", userDetails);

        assertThat(valid).isFalse();
    }

    @Test
    @DisplayName("should return false for token with different user")
    void isTokenValid_differentUser_returnsFalse() {
        String token = jwtService.generateAccessToken(userDetails);
        UserDetails differentUser = new User("otheruser", "password",
                Collections.emptyList());

        boolean valid = jwtService.isTokenValid(token, differentUser);

        assertThat(valid).isFalse();
    }

    @Test
    @DisplayName("should return false for expired token")
    void isTokenValid_expiredToken_returnsFalse() {
        String token = jwtService.generateToken(new HashMap<>(), userDetails, -1000L);

        boolean valid = jwtService.isTokenValid(token, userDetails);

        assertThat(valid).isFalse();
    }

    @Test
    @DisplayName("should return correct access token expiration")
    void getAccessTokenExpiration_returnsConfiguredValue() {
        assertThat(jwtService.getAccessTokenExpiration()).isEqualTo(900000L);
    }

    @Test
    @DisplayName("should return correct refresh token expiration")
    void getRefreshTokenExpiration_returnsConfiguredValue() {
        assertThat(jwtService.getRefreshTokenExpiration()).isEqualTo(604800000L);
    }

    @Test
    @DisplayName("should extract claim from token")
    void extractClaim_validToken_returnsClaim() {
        String token = jwtService.generateAccessToken(userDetails);

        String subject = jwtService.extractClaim(token, Claims::getSubject);

        assertThat(subject).isEqualTo("testuser");
    }

    @Test
    @DisplayName("should generate token with custom claims")
    void generateToken_withExtraClaims_returnsToken() {
        java.util.Map<String, Object> claims = new java.util.HashMap<>();
        claims.put("role", "ADMIN");

        String token = jwtService.generateToken(claims, userDetails, 900000L);

        assertThat(token).isNotNull();
        String extractedRole = jwtService.extractClaim(token, c -> c.get("role", String.class));
        assertThat(extractedRole).isEqualTo("ADMIN");
    }
}
