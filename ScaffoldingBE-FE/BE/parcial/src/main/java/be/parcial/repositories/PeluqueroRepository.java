package be.parcial.repositories;

import be.parcial.domain.entities.PeluqueroEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PeluqueroRepository extends JpaRepository<PeluqueroEntity, Long> {
    List<PeluqueroEntity> findByActivoTrue();
    Optional<PeluqueroEntity> findByUserUsername(String username);
}
