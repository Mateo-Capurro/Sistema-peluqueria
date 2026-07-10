package be.parcial.services;

import be.parcial.dtos.SlotDTO;
import be.parcial.services.disponibilidad.FixedStepSlotStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class FixedStepSlotStrategyTest {

    private FixedStepSlotStrategy strategy;
    private LocalDate fecha;

    @BeforeEach
    void setUp() {
        strategy = new FixedStepSlotStrategy();
        fecha = LocalDate.of(2026, 7, 14); // martes
    }

    @Test
    @DisplayName("generates step-15 slots that fit inside the jornada")
    void calcular_emptyOccupied_generatesSlots() {
        List<SlotDTO> slots = strategy.calcular(
                fecha, LocalTime.of(9, 0), LocalTime.of(11, 0), 30,
                List.of(), fecha.atStartOfDay());

        // 09:00..10:30 start times, step 15 -> 7 slots
        assertThat(slots).hasSize(7);
        assertThat(slots.get(0).getInicio()).isEqualTo(LocalDateTime.of(fecha, LocalTime.of(9, 0)));
        assertThat(slots.get(0).getFin()).isEqualTo(LocalDateTime.of(fecha, LocalTime.of(9, 30)));
    }

    @Test
    @DisplayName("excludes candidates overlapping an occupied interval")
    void calcular_withOccupied_excludesOverlaps() {
        SlotDTO ocupado = new SlotDTO(
                LocalDateTime.of(fecha, LocalTime.of(9, 0)),
                LocalDateTime.of(fecha, LocalTime.of(9, 30)));

        List<SlotDTO> slots = strategy.calcular(
                fecha, LocalTime.of(9, 0), LocalTime.of(11, 0), 30,
                List.of(ocupado), fecha.atStartOfDay());

        // 09:00 and 09:15 overlap the occupied block -> excluded (7 - 2 = 5)
        assertThat(slots).hasSize(5);
        assertThat(slots.get(0).getInicio()).isEqualTo(LocalDateTime.of(fecha, LocalTime.of(9, 30)));
    }

    @Test
    @DisplayName("excludes candidates that start in the past")
    void calcular_pastCandidates_excluded() {
        List<SlotDTO> slots = strategy.calcular(
                fecha, LocalTime.of(9, 0), LocalTime.of(11, 0), 30,
                List.of(), LocalDateTime.of(fecha, LocalTime.of(9, 30)));

        // 09:00 and 09:15 are before "ahora" -> excluded (7 - 2 = 5)
        assertThat(slots).hasSize(5);
        assertThat(slots.get(0).getInicio()).isEqualTo(LocalDateTime.of(fecha, LocalTime.of(9, 30)));
    }

    @Test
    @DisplayName("keeps candidates that end before an occupied interval starts")
    void calcular_occupiedAfterCandidate_keepsCandidate() {
        // occupied in the afternoon: early candidates start before o.fin but end before o.inicio
        SlotDTO ocupado = new SlotDTO(
                LocalDateTime.of(fecha, LocalTime.of(10, 30)),
                LocalDateTime.of(fecha, LocalTime.of(11, 0)));

        List<SlotDTO> slots = strategy.calcular(
                fecha, LocalTime.of(9, 0), LocalTime.of(10, 0), 30,
                List.of(ocupado), fecha.atStartOfDay());

        // jornada 09:00-10:00, dur 30 -> starts 09:00/09:15/09:30; none overlaps the 10:30 block
        assertThat(slots).hasSize(3);
    }

    @Test
    @DisplayName("returns empty when tratamiento does not fit in the jornada")
    void calcular_durationTooLong_returnsEmpty() {
        List<SlotDTO> slots = strategy.calcular(
                fecha, LocalTime.of(9, 0), LocalTime.of(10, 0), 120,
                List.of(), fecha.atStartOfDay());

        assertThat(slots).isEmpty();
    }
}
