package be.parcial.repositories;

import be.parcial.domain.entities.RefreshTokenEntity;
import be.parcial.domain.entities.UserEntity;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshTokenEntity, Long> {
    @EntityGraph(attributePaths = {"user"})
    Optional<RefreshTokenEntity> findByToken(String token);
    void deleteByUser(UserEntity user);
}
