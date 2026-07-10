package be.parcial.repositories;

import be.parcial.domain.entities.TratamientoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TratamientoRepository extends JpaRepository<TratamientoEntity, Long> {
    List<TratamientoEntity> findByActivoTrue();
    boolean existsByNombre(String nombre);
}
