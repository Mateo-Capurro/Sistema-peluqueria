package be.parcial.services.implementations;

import be.parcial.domain.entities.RefreshTokenEntity;
import be.parcial.domain.entities.UserEntity;
import be.parcial.exceptions.Messages;
import be.parcial.repositories.RefreshTokenRepository;
import be.parcial.security.JwtService;
import be.parcial.services.RefreshTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class RefreshTokenServiceImplementation implements RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtService jwtService;

    @Override
    public RefreshTokenEntity createRefreshToken(UserEntity user) {
        RefreshTokenEntity refreshToken = new RefreshTokenEntity();
        refreshToken.setToken(jwtService.generateRefreshToken(
                new org.springframework.security.core.userdetails.User(
                        user.getUsername(),
                        user.getPassword(),
                        java.util.Collections.singletonList(
                                new org.springframework.security.core.authority.SimpleGrantedAuthority(
                                        "ROLE_" + user.getRole().name()))
                )
        ));
        refreshToken.setUser(user);
        refreshToken.setExpiryDate(LocalDateTime.now().plusSeconds(
                jwtService.getRefreshTokenExpiration() / 1000));
        refreshToken.setRevoked(false);
        return refreshTokenRepository.save(refreshToken);
    }

    @Override
    public RefreshTokenEntity validateRefreshToken(String token) {
        RefreshTokenEntity refreshToken = refreshTokenRepository.findByToken(token)
                .orElseThrow(() -> new IllegalArgumentException(Messages.REFRESH_TOKEN_NOT_FOUND));

        if (refreshToken.isRevoked()) {
            throw new IllegalArgumentException(Messages.REFRESH_TOKEN_REVOKED);
        }

        if (refreshToken.isExpired()) {
            throw new IllegalArgumentException(Messages.REFRESH_TOKEN_EXPIRED);
        }

        return refreshToken;
    }

    @Override
    public void revokeRefreshToken(String token) {
        RefreshTokenEntity refreshToken = refreshTokenRepository.findByToken(token)
                .orElseThrow(() -> new IllegalArgumentException(Messages.REFRESH_TOKEN_NOT_FOUND));
        refreshToken.setRevoked(true);
        refreshTokenRepository.save(refreshToken);
    }
}
