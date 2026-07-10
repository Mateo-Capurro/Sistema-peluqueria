package be.parcial;

import be.parcial.domain.entities.EstadoTurno;
import be.parcial.domain.entities.PeluqueroEntity;
import be.parcial.domain.entities.TratamientoEntity;
import be.parcial.domain.entities.TurnoEntity;
import be.parcial.dtos.DisponibilidadResponseDTO;
import be.parcial.dtos.ReservaTurnoRequestDTO;
import be.parcial.dtos.TurnoResponseDTO;
import be.parcial.repositories.PeluqueroRepository;
import be.parcial.repositories.TratamientoRepository;
import be.parcial.repositories.TurnoRepository;
import be.parcial.services.DisponibilidadService;
import be.parcial.services.TurnoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = ParcialApplication.class)
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class TurnoIntegrationTest {

    @Autowired private TurnoService turnoService;
    @Autowired private DisponibilidadService disponibilidadService;
    @Autowired private PeluqueroRepository peluqueroRepository;
    @Autowired private TratamientoRepository tratamientoRepository;
    @Autowired private TurnoRepository turnoRepository;

    private Long peluqueroId;
    private Long tratamientoId;
    private LocalDateTime inicio;

    @BeforeEach
    void setUp() {
        PeluqueroEntity peluquero = peluqueroRepository.findByUserUsername("mrodriguez").orElseThrow();
        peluqueroId = peluquero.getId();
        TratamientoEntity tratamiento = tratamientoRepository.findByActivoTrue().stream()
                .filter(t -> t.getNombre().equals("Corte pelo corto")).findFirst().orElseThrow();
        tratamientoId = tratamiento.getId();
        // a future Tuesday at 10:00, well within the 09:00-18:00 jornada
        LocalDate martes = LocalDate.now().plusMonths(1).with(TemporalAdjusters.next(DayOfWeek.TUESDAY));
        inicio = martes.atTime(10, 0);
    }

    @Test
    @DisplayName("end-to-end: reservar -> confirmar -> cancelar persists estado")
    void reservar_confirmar_cancelar_persists() {
        ReservaTurnoRequestDTO request =
                new ReservaTurnoRequestDTO(peluqueroId, tratamientoId, inicio);

        TurnoResponseDTO reservado = turnoService.reservar("user", request);
        assertThat(reservado.getId()).isNotNull();
        assertThat(reservado.getEstado()).isEqualTo(EstadoTurno.PENDIENTE);
        assertThat(reservado.getFin()).isEqualTo(inicio.plusMinutes(30));

        TurnoResponseDTO confirmado = turnoService.confirmar("user", reservado.getId());
        assertThat(confirmado.getEstado()).isEqualTo(EstadoTurno.CONFIRMADO);

        TurnoResponseDTO cancelado = turnoService.cancelar("user", reservado.getId());
        assertThat(cancelado.getEstado()).isEqualTo(EstadoTurno.CANCELADO);

        TurnoEntity persisted = turnoRepository.findById(reservado.getId()).orElseThrow();
        assertThat(persisted.getEstado()).isEqualTo(EstadoTurno.CANCELADO);
    }

    @Test
    @DisplayName("a reserved slot no longer appears in disponibilidad")
    void reservar_removesSlotFromDisponibilidad() {
        LocalDate fecha = inicio.toLocalDate();
        DisponibilidadResponseDTO before =
                disponibilidadService.calcular(peluqueroId, fecha, tratamientoId);
        assertThat(before.getSlots()).isNotEmpty();

        LocalDateTime slot = before.getSlots().get(0).getInicio();
        turnoService.reservar("user", new ReservaTurnoRequestDTO(peluqueroId, tratamientoId, slot));

        DisponibilidadResponseDTO after =
                disponibilidadService.calcular(peluqueroId, fecha, tratamientoId);
        boolean stillAvailable = after.getSlots().stream()
                .anyMatch(s -> s.getInicio().equals(slot));
        assertThat(stillAvailable).isFalse();
        assertThat(after.getSlots()).hasSizeLessThan(before.getSlots().size());
    }
}
