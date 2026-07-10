package be.parcial.services.implementations;

import be.parcial.domain.entities.*;
import be.parcial.dtos.ReservaTurnoRequestDTO;
import be.parcial.dtos.TurnoResponseDTO;
import be.parcial.exceptions.Messages;
import be.parcial.exceptions.ResourceNotFoundException;
import be.parcial.exceptions.SlotNoDisponibleException;
import be.parcial.repositories.PeluqueroRepository;
import be.parcial.repositories.TratamientoRepository;
import be.parcial.repositories.TurnoRepository;
import be.parcial.repositories.UserRepository;
import be.parcial.services.NotificationService;
import be.parcial.services.TurnoService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class TurnoServiceImplementation implements TurnoService {

    private static final List<EstadoTurno> ACTIVOS =
            List.of(EstadoTurno.PENDIENTE, EstadoTurno.CONFIRMADO);

    private final TurnoRepository turnoRepository;
    private final UserRepository userRepository;
    private final PeluqueroRepository peluqueroRepository;
    private final TratamientoRepository tratamientoRepository;
    private final NotificationService notificationService;
    private final Clock clock;

    @Override
    public TurnoResponseDTO reservar(String clienteUsername, ReservaTurnoRequestDTO request) {
        UserEntity cliente = userRepository.findByUsername(clienteUsername)
                .orElseThrow(() -> new ResourceNotFoundException(
                        String.format(Messages.USER_NOT_FOUND, clienteUsername)));
        PeluqueroEntity peluquero = peluqueroRepository.findById(request.getPeluqueroId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        String.format(Messages.PELUQUERO_NOT_FOUND, request.getPeluqueroId())));
        TratamientoEntity tratamiento = tratamientoRepository.findById(request.getTratamientoId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        String.format(Messages.TRATAMIENTO_NOT_FOUND, request.getTratamientoId())));

        LocalDateTime inicio = request.getInicio();
        LocalDateTime fin = inicio.plusMinutes(tratamiento.getDuracionMinutos());

        validarFechaFutura(inicio);
        validarDiaLaborable(inicio);
        validarJornada(inicio, fin, peluquero);
        validarSinSolape(peluquero.getId(), inicio, fin);

        TurnoEntity turno = new TurnoEntity();
        turno.setCliente(cliente);
        turno.setPeluquero(peluquero);
        turno.setTratamiento(tratamiento);
        turno.setInicio(inicio);
        turno.setFin(fin);
        turno.setEstado(EstadoTurno.PENDIENTE);

        TurnoEntity saved = turnoRepository.save(turno);
        notificationService.notifyReserva(saved);
        return toDto(saved);
    }

    @Override
    public List<TurnoResponseDTO> misTurnos(String clienteUsername) {
        UserEntity cliente = userRepository.findByUsername(clienteUsername)
                .orElseThrow(() -> new ResourceNotFoundException(
                        String.format(Messages.USER_NOT_FOUND, clienteUsername)));
        return turnoRepository.findByClienteIdOrderByInicioDesc(cliente.getId()).stream()
                .map(this::toDto)
                .toList();
    }

    @Override
    public List<TurnoResponseDTO> agenda(String peluqueroUsername) {
        PeluqueroEntity peluquero = peluqueroRepository.findByUserUsername(peluqueroUsername)
                .orElseThrow(() -> new ResourceNotFoundException(
                        String.format(Messages.USER_NOT_FOUND, peluqueroUsername)));
        return turnoRepository.findByPeluqueroIdOrderByInicioAsc(peluquero.getId()).stream()
                .map(this::toDto)
                .toList();
    }

    @Override
    public TurnoResponseDTO confirmar(String username, Long turnoId) {
        TurnoEntity turno = getTurno(turnoId);
        if (!esCliente(turno, username)) {
            throw new AccessDeniedException(Messages.ACCESS_DENIED);
        }
        turno.setEstado(turno.getEstado().confirmar());
        return toDto(turnoRepository.save(turno));
    }

    @Override
    public TurnoResponseDTO cancelar(String username, Long turnoId) {
        TurnoEntity turno = getTurno(turnoId);
        if (!esCliente(turno, username) && !esPeluquero(turno, username)) {
            throw new AccessDeniedException(Messages.ACCESS_DENIED);
        }
        turno.setEstado(turno.getEstado().cancelar());
        return toDto(turnoRepository.save(turno));
    }

    @Override
    public TurnoResponseDTO completar(String username, Long turnoId) {
        TurnoEntity turno = getTurno(turnoId);
        if (!esPeluquero(turno, username)) {
            throw new AccessDeniedException(Messages.ACCESS_DENIED);
        }
        turno.setEstado(turno.getEstado().completar());
        return toDto(turnoRepository.save(turno));
    }

    private void validarFechaFutura(LocalDateTime inicio) {
        if (!inicio.isAfter(LocalDateTime.now(clock))) {
            throw new SlotNoDisponibleException(Messages.SLOT_FECHA_PASADA);
        }
    }

    private void validarDiaLaborable(LocalDateTime inicio) {
        if (inicio.getDayOfWeek() == DayOfWeek.MONDAY) {
            throw new SlotNoDisponibleException(Messages.SLOT_DIA_NO_LABORABLE);
        }
    }

    private void validarJornada(LocalDateTime inicio, LocalDateTime fin, PeluqueroEntity peluquero) {
        LocalTime ti = inicio.toLocalTime();
        LocalTime tf = fin.toLocalTime();
        boolean dentro = !ti.isBefore(peluquero.getHoraInicio())
                && !tf.isAfter(peluquero.getHoraFin())
                && fin.toLocalDate().isEqual(inicio.toLocalDate());
        if (!dentro) {
            throw new SlotNoDisponibleException(Messages.SLOT_FUERA_JORNADA);
        }
    }

    private void validarSinSolape(Long peluqueroId, LocalDateTime inicio, LocalDateTime fin) {
        boolean ocupado = turnoRepository
                .existsByPeluqueroIdAndEstadoInAndInicioLessThanAndFinGreaterThan(
                        peluqueroId, ACTIVOS, fin, inicio);
        if (ocupado) {
            throw new SlotNoDisponibleException(Messages.SLOT_OCUPADO);
        }
    }

    private TurnoEntity getTurno(Long id) {
        return turnoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        String.format(Messages.TURNO_NOT_FOUND, id)));
    }

    private boolean esCliente(TurnoEntity turno, String username) {
        return turno.getCliente().getUsername().equals(username);
    }

    private boolean esPeluquero(TurnoEntity turno, String username) {
        return turno.getPeluquero().getUser().getUsername().equals(username);
    }

    private TurnoResponseDTO toDto(TurnoEntity turno) {
        return new TurnoResponseDTO(
                turno.getId(),
                turno.getCliente().getName(),
                turno.getPeluquero().getUser().getName(),
                turno.getTratamiento().getNombre(),
                turno.getInicio(),
                turno.getFin(),
                turno.getEstado());
    }
}
