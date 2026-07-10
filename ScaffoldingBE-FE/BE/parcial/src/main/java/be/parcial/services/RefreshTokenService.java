package be.parcial.services;

import be.parcial.domain.entities.RefreshTokenEntity;
import be.parcial.domain.entities.UserEntity;

public interface RefreshTokenService {
    RefreshTokenEntity createRefreshToken(UserEntity user);
    RefreshTokenEntity validateRefreshToken(String token);
    void revokeRefreshToken(String token);
}
