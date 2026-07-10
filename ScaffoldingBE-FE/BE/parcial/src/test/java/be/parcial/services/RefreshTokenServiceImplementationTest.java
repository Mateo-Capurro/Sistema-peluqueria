package be.parcial.services;

import be.parcial.domain.entities.RefreshTokenEntity;
import be.parcial.domain.entities.UserEntity;
import be.parcial.exceptions.Messages;
import be.parcial.repositories.RefreshTokenRepository;
import be.parcial.security.JwtService;
import be.parcial.services.implementations.RefreshTokenServiceImplementation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RefreshTokenServiceImplementationTest {

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private RefreshTokenServiceImplementation refreshTokenService;

    private UserEntity userEntity;
    private RefreshTokenEntity refreshTokenEntity;

    @BeforeEach
    void setUp() {
        userEntity = new UserEntity();
        userEntity.setId(1L);
        userEntity.setUsername("testuser");
        userEntity.setPassword("encodedPassword");
        userEntity.setName("Test User");
        userEntity.setRole(UserEntity.Role.CLIENTE);

        refreshTokenEntity = new RefreshTokenEntity();
        refreshTokenEntity.setId(1L);
        refreshTokenEntity.setToken("valid-refresh-token");
        refreshTokenEntity.setUser(userEntity);
        refreshTokenEntity.setExpiryDate(LocalDateTime.now().plusDays(7));
        refreshTokenEntity.setRevoked(false);
    }

    @Nested
    @DisplayName("createRefreshToken")
    class CreateRefreshToken {

        @Test
        @DisplayName("should create refresh token successfully")
        void createRefreshToken_validUser_returnsToken() {
            when(jwtService.generateRefreshToken(any())).thenReturn("generated-token");
            when(jwtService.getRefreshTokenExpiration()).thenReturn(604800000L);
            when(refreshTokenRepository.save(any(RefreshTokenEntity.class)))
                    .thenReturn(refreshTokenEntity);

            RefreshTokenEntity result = refreshTokenService.createRefreshToken(userEntity);

            assertThat(result).isNotNull();
            verify(refreshTokenRepository).save(any(RefreshTokenEntity.class));
        }
    }

    @Nested
    @DisplayName("validateRefreshToken")
    class ValidateRefreshToken {

        @Test
        @DisplayName("should validate valid token")
        void validateRefreshToken_validToken_returnsToken() {
            when(refreshTokenRepository.findByToken("valid-refresh-token"))
                    .thenReturn(Optional.of(refreshTokenEntity));

            RefreshTokenEntity result = refreshTokenService.validateRefreshToken("valid-refresh-token");

            assertThat(result).isNotNull();
            assertThat(result.getToken()).isEqualTo("valid-refresh-token");
        }

        @Test
        @DisplayName("should throw when token not found")
        void validateRefreshToken_notFound_throwsException() {
            when(refreshTokenRepository.findByToken("not-found"))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> refreshTokenService.validateRefreshToken("not-found"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage(Messages.REFRESH_TOKEN_NOT_FOUND);
        }

        @Test
        @DisplayName("should throw when token revoked")
        void validateRefreshToken_revoked_throwsException() {
            refreshTokenEntity.setRevoked(true);
            when(refreshTokenRepository.findByToken("revoked-token"))
                    .thenReturn(Optional.of(refreshTokenEntity));

            assertThatThrownBy(() -> refreshTokenService.validateRefreshToken("revoked-token"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage(Messages.REFRESH_TOKEN_REVOKED);
        }

        @Test
        @DisplayName("should throw when token expired")
        void validateRefreshToken_expired_throwsException() {
            refreshTokenEntity.setExpiryDate(LocalDateTime.now().minusDays(1));
            when(refreshTokenRepository.findByToken("expired-token"))
                    .thenReturn(Optional.of(refreshTokenEntity));

            assertThatThrownBy(() -> refreshTokenService.validateRefreshToken("expired-token"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage(Messages.REFRESH_TOKEN_EXPIRED);
        }
    }

    @Nested
    @DisplayName("revokeRefreshToken")
    class RevokeRefreshToken {

        @Test
        @DisplayName("should revoke token successfully")
        void revokeRefreshToken_validToken_revokes() {
            when(refreshTokenRepository.findByToken("valid-token"))
                    .thenReturn(Optional.of(refreshTokenEntity));
            when(refreshTokenRepository.save(any(RefreshTokenEntity.class)))
                    .thenReturn(refreshTokenEntity);

            refreshTokenService.revokeRefreshToken("valid-token");

            assertThat(refreshTokenEntity.isRevoked()).isTrue();
            verify(refreshTokenRepository).save(refreshTokenEntity);
        }

        @Test
        @DisplayName("should throw when token not found for revoke")
        void revokeRefreshToken_notFound_throwsException() {
            when(refreshTokenRepository.findByToken("not-found"))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> refreshTokenService.revokeRefreshToken("not-found"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage(Messages.REFRESH_TOKEN_NOT_FOUND);
        }
    }
}
