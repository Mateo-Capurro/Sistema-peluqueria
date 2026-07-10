package be.parcial.services.implementations;

import be.parcial.domain.entities.EstadoTurno;
import be.parcial.domain.entities.PeluqueroEntity;
import be.parcial.domain.entities.TratamientoEntity;
import be.parcial.dtos.DisponibilidadResponseDTO;
import be.parcial.dtos.SlotDTO;
import be.parcial.exceptions.Messages;
import be.parcial.exceptions.ResourceNotFoundException;
import be.parcial.repositories.PeluqueroRepository;
import be.parcial.repositories.TratamientoRepository;
import be.parcial.repositories.TurnoRepository;
import be.parcial.services.DisponibilidadService;
import be.parcial.services.disponibilidad.SlotCalculationStrategy;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DisponibilidadServiceImplementation implements DisponibilidadService {

    private static final List<EstadoTurno> ACTIVOS =
            List.of(EstadoTurno.PENDIENTE, EstadoTurno.CONFIRMADO);

    private final PeluqueroRepository peluqueroRepository;
    private final TratamientoRepository tratamientoRepository;
    private final TurnoRepository turnoRepository;
    private final SlotCalculationStrategy slotStrategy;

    @Override
    public DisponibilidadResponseDTO calcular(Long peluqueroId, LocalDate fecha, Long tratamientoId) {
        PeluqueroEntity peluquero = peluqueroRepository.findById(peluqueroId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        String.format(Messages.PELUQUERO_NOT_FOUND, peluqueroId)));
        TratamientoEntity tratamiento = tratamientoRepository.findById(tratamientoId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        String.format(Messages.TRATAMIENTO_NOT_FOUND, tratamientoId)));

        if (fecha.getDayOfWeek() == DayOfWeek.MONDAY) {
            return new DisponibilidadResponseDTO(peluqueroId, fecha, List.of());
        }

        List<SlotDTO> ocupados = turnoRepository
                .findByPeluqueroIdAndEstadoInAndInicioBetween(
                        peluqueroId, ACTIVOS, fecha.atStartOfDay(), fecha.atTime(LocalTime.MAX))
                .stream()
                .map(turno -> new SlotDTO(turno.getInicio(), turno.getFin()))
                .toList();

        List<SlotDTO> slots = slotStrategy.calcular(
                fecha,
                peluquero.getHoraInicio(),
                peluquero.getHoraFin(),
                tratamiento.getDuracionMinutos(),
                ocupados,
                LocalDateTime.now());

        return new DisponibilidadResponseDTO(peluqueroId, fecha, slots);
    }
}
