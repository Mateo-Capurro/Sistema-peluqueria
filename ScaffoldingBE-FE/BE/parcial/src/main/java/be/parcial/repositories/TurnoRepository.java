package be.parcial.repositories;

import be.parcial.domain.entities.EstadoTurno;
import be.parcial.domain.entities.TurnoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface TurnoRepository extends JpaRepository<TurnoEntity, Long> {

    /**
     * True if the peluquero already has a turno (in one of the given estados)
     * whose interval overlaps [inicio, fin): existing.inicio < fin AND existing.fin > inicio.
     */
    boolean existsByPeluqueroIdAndEstadoInAndInicioLessThanAndFinGreaterThan(
            Long peluqueroId, Collection<EstadoTurno> estados, LocalDateTime fin, LocalDateTime inicio);

    List<TurnoEntity> findByPeluqueroIdAndEstadoInAndInicioBetween(
            Long peluqueroId, Collection<EstadoTurno> estados, LocalDateTime desde, LocalDateTime hasta);

    List<TurnoEntity> findByClienteIdOrderByInicioDesc(Long clienteId);

    List<TurnoEntity> findByPeluqueroIdOrderByInicioAsc(Long peluqueroId);

    Optional<TurnoEntity> findByConfirmToken(String confirmToken);
}
