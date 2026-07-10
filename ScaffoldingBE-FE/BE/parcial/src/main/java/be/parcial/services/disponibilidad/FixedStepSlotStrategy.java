package be.parcial.services.disponibilidad;

import be.parcial.dtos.SlotDTO;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Generates candidate slots stepping by a fixed interval from the start of the
 * jornada, keeping only those that fit before the jornada ends, are not in the
 * past, and do not overlap an occupied interval.
 */
@Component
public class FixedStepSlotStrategy implements SlotCalculationStrategy {

    private static final int STEP_MINUTES = 15;

    @Override
    public List<SlotDTO> calcular(LocalDate fecha,
                                  LocalTime jornadaInicio,
                                  LocalTime jornadaFin,
                                  int duracionMinutos,
                                  List<SlotDTO> ocupados,
                                  LocalDateTime ahora) {
        List<SlotDTO> slots = new ArrayList<>();
        LocalDateTime finJornada = LocalDateTime.of(fecha, jornadaFin);
        LocalDateTime candidato = LocalDateTime.of(fecha, jornadaInicio);

        while (!candidato.plusMinutes(duracionMinutos).isAfter(finJornada)) {
            LocalDateTime finCandidato = candidato.plusMinutes(duracionMinutos);
            if (!candidato.isBefore(ahora) && !solapa(candidato, finCandidato, ocupados)) {
                slots.add(new SlotDTO(candidato, finCandidato));
            }
            candidato = candidato.plusMinutes(STEP_MINUTES);
        }
        return slots;
    }

    private boolean solapa(LocalDateTime inicio, LocalDateTime fin, List<SlotDTO> ocupados) {
        return ocupados.stream()
                .anyMatch(o -> inicio.isBefore(o.getFin()) && fin.isAfter(o.getInicio()));
    }
}
