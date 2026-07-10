package be.parcial.services.disponibilidad;

import be.parcial.dtos.SlotDTO;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

/**
 * Strategy for computing the free slots of a peluquero on a given date.
 */
public interface SlotCalculationStrategy {

    List<SlotDTO> calcular(LocalDate fecha,
                           LocalTime jornadaInicio,
                           LocalTime jornadaFin,
                           int duracionMinutos,
                           List<SlotDTO> ocupados,
                           LocalDateTime ahora);
}
