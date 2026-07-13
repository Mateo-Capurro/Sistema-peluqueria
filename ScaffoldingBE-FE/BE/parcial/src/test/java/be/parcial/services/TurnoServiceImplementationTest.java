package be.parcial.services;

import be.parcial.domain.entities.*;
import be.parcial.dtos.ReservaTurnoRequestDTO;
import be.parcial.dtos.TurnoResponseDTO;
import be.parcial.exceptions.ResourceNotFoundException;
import be.parcial.exceptions.SlotNoDisponibleException;
import be.parcial.exceptions.TransicionInvalidaException;
import be.parcial.repositories.PeluqueroRepository;
import be.parcial.repositories.TratamientoRepository;
import be.parcial.repositories.TurnoRepository;
import be.parcial.repositories.UserRepository;
import be.parcial.services.implementations.TurnoServiceImplementation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.math.BigDecimal;
import java.time.*;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TurnoServiceImplementationTest {

    @Mock private TurnoRepository turnoRepository;
    @Mock private UserRepository userRepository;
    @Mock private PeluqueroRepository peluqueroRepository;
    @Mock private TratamientoRepository tratamientoRepository;
    @Mock private NotificationService notificationService;

    private TurnoServiceImplementation turnoService;

    private UserEntity clienteUser;
    private UserEntity peluqueroUser;
    private PeluqueroEntity peluquero;
    private TratamientoEntity tratamiento;

    // now fixed at 2026-07-10T12:00 (Friday)
    private final Clock clock =
            Clock.fixed(LocalDateTime.of(2026, 7, 10, 12, 0).toInstant(ZoneOffset.UTC), ZoneOffset.UTC);
    private final LocalDateTime martes10 = LocalDateTime.of(2026, 7, 14, 10, 0);

    @BeforeEach
    void setUp() {
        turnoService = new TurnoServiceImplementation(turnoRepository, userRepository,
                peluqueroRepository, tratamientoRepository, notificationService, clock);

        clienteUser = new UserEntity();
        clienteUser.setId(1L);
        clienteUser.setUsername("juan");
        clienteUser.setName("Juan Perez");

        peluqueroUser = new UserEntity();
        peluqueroUser.setId(2L);
        peluqueroUser.setUsername("marta");
        peluqueroUser.setName("Marta Rodriguez");

        peluquero = new PeluqueroEntity(5L, peluqueroUser, LocalTime.of(9, 0), LocalTime.of(18, 0), true);
        tratamiento = new TratamientoEntity(7L, "Corte", 30, new BigDecimal("4000.00"), true);
    }

    private ReservaTurnoRequestDTO reserva(LocalDateTime inicio) {
        return new ReservaTurnoRequestDTO(5L, 7L, inicio);
    }

    private void stubEntities() {
        when(userRepository.findByUsername("juan")).thenReturn(Optional.of(clienteUser));
        when(peluqueroRepository.findById(5L)).thenReturn(Optional.of(peluquero));
        when(tratamientoRepository.findById(7L)).thenReturn(Optional.of(tratamiento));
    }

    private TurnoEntity turnoEnEstado(EstadoTurno estado) {
        TurnoEntity turno = new TurnoEntity();
        turno.setId(100L);
        turno.setCliente(clienteUser);
        turno.setPeluquero(peluquero);
        turno.setTratamiento(tratamiento);
        turno.setInicio(martes10);
        turno.setFin(martes10.plusMinutes(30));
        turno.setEstado(estado);
        return turno;
    }

    @Nested
    @DisplayName("reservar")
    class Reservar {

        @Test
        @DisplayName("valid reservation creates PENDIENTE and notifies")
        void reservar_valid_createsAndNotifies() {
            stubEntities();
            when(turnoRepository.existsByPeluqueroIdAndEstadoInAndInicioLessThanAndFinGreaterThan(
                    eq(5L), anyCollection(), any(), any())).thenReturn(false);
            when(turnoRepository.save(any(TurnoEntity.class))).thenAnswer(inv -> inv.getArgument(0));

            TurnoResponseDTO result = turnoService.reservar("juan", reserva(martes10));

            assertThat(result.getEstado()).isEqualTo(EstadoTurno.PENDIENTE);
            assertThat(result.getInicio()).isEqualTo(martes10);
            assertThat(result.getFin()).isEqualTo(martes10.plusMinutes(30));
            assertThat(result.getClienteNombre()).isEqualTo("Juan Perez");
            assertThat(result.getPeluqueroNombre()).isEqualTo("Marta Rodriguez");
            verify(notificationService).notifyReserva(any(TurnoEntity.class));
        }

        @Test
        @DisplayName("throws 404 when cliente not found")
        void reservar_clienteNotFound_throws() {
            when(userRepository.findByUsername("juan")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> turnoService.reservar("juan", reserva(martes10)))
                    .isInstanceOf(ResourceNotFoundException.class);
        }

        @Test
        @DisplayName("throws 404 when peluquero not found")
        void reservar_peluqueroNotFound_throws() {
            when(userRepository.findByUsername("juan")).thenReturn(Optional.of(clienteUser));
            when(peluqueroRepository.findById(5L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> turnoService.reservar("juan", reserva(martes10)))
                    .isInstanceOf(ResourceNotFoundException.class);
        }

        @Test
        @DisplayName("throws 404 when tratamiento not found")
        void reservar_tratamientoNotFound_throws() {
            when(userRepository.findByUsername("juan")).thenReturn(Optional.of(clienteUser));
            when(peluqueroRepository.findById(5L)).thenReturn(Optional.of(peluquero));
            when(tratamientoRepository.findById(7L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> turnoService.reservar("juan", reserva(martes10)))
                    .isInstanceOf(ResourceNotFoundException.class);
        }

        @Test
        @DisplayName("rejects a past datetime")
        void reservar_past_throws() {
            stubEntities();

            assertThatThrownBy(() -> turnoService.reservar("juan",
                    reserva(LocalDateTime.of(2026, 7, 9, 10, 0))))
                    .isInstanceOf(SlotNoDisponibleException.class)
                    .hasMessageContaining("pasado");
        }

        @Test
        @DisplayName("rejects Monday")
        void reservar_monday_throws() {
            stubEntities();

            assertThatThrownBy(() -> turnoService.reservar("juan",
                    reserva(LocalDateTime.of(2026, 7, 13, 10, 0))))
                    .isInstanceOf(SlotNoDisponibleException.class)
                    .hasMessageContaining("lunes");
        }

        @Test
        @DisplayName("rejects start before jornada")
        void reservar_beforeJornada_throws() {
            stubEntities();

            assertThatThrownBy(() -> turnoService.reservar("juan",
                    reserva(LocalDateTime.of(2026, 7, 14, 8, 0))))
                    .isInstanceOf(SlotNoDisponibleException.class)
                    .hasMessageContaining("jornada");
        }

        @Test
        @DisplayName("rejects end after jornada")
        void reservar_endAfterJornada_throws() {
            stubEntities();

            assertThatThrownBy(() -> turnoService.reservar("juan",
                    reserva(LocalDateTime.of(2026, 7, 14, 17, 45))))
                    .isInstanceOf(SlotNoDisponibleException.class)
                    .hasMessageContaining("jornada");
        }

        @Test
        @DisplayName("rejects a slot that crosses midnight")
        void reservar_crossesDay_throws() {
            stubEntities();

            assertThatThrownBy(() -> turnoService.reservar("juan",
                    reserva(LocalDateTime.of(2026, 7, 14, 23, 30))))
                    .isInstanceOf(SlotNoDisponibleException.class)
                    .hasMessageContaining("jornada");
        }

        @Test
        @DisplayName("rejects an overlapping slot")
        void reservar_overlap_throws() {
            stubEntities();
            when(turnoRepository.existsByPeluqueroIdAndEstadoInAndInicioLessThanAndFinGreaterThan(
                    eq(5L), anyCollection(), any(), any())).thenReturn(true);

            assertThatThrownBy(() -> turnoService.reservar("juan", reserva(martes10)))
                    .isInstanceOf(SlotNoDisponibleException.class)
                    .hasMessageContaining("horario");
            verify(notificationService, never()).notifyReserva(any());
        }
    }

    @Nested
    @DisplayName("listados")
    class Listados {

        @Test
        void misTurnos_returnsClienteTurnos() {
            when(userRepository.findByUsername("juan")).thenReturn(Optional.of(clienteUser));
            when(turnoRepository.findByClienteIdOrderByInicioDesc(1L))
                    .thenReturn(List.of(turnoEnEstado(EstadoTurno.PENDIENTE)));

            List<TurnoResponseDTO> result = turnoService.misTurnos("juan");

            assertThat(result).hasSize(1);
        }

        @Test
        void misTurnos_clienteNotFound_throws() {
            when(userRepository.findByUsername("juan")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> turnoService.misTurnos("juan"))
                    .isInstanceOf(ResourceNotFoundException.class);
        }

        @Test
        void agenda_returnsPeluqueroTurnos() {
            when(peluqueroRepository.findByUserUsername("marta")).thenReturn(Optional.of(peluquero));
            when(turnoRepository.findByPeluqueroIdOrderByInicioAsc(5L))
                    .thenReturn(List.of(turnoEnEstado(EstadoTurno.CONFIRMADO)));

            assertThat(turnoService.agenda("marta")).hasSize(1);
        }

        @Test
        void agenda_peluqueroNotFound_throws() {
            when(peluqueroRepository.findByUserUsername("marta")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> turnoService.agenda("marta"))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("transiciones")
    class Transiciones {

        @Test
        void confirmar_owner_movesToConfirmado() {
            when(turnoRepository.findById(100L)).thenReturn(Optional.of(turnoEnEstado(EstadoTurno.PENDIENTE)));
            when(turnoRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            assertThat(turnoService.confirmar("juan", 100L).getEstado()).isEqualTo(EstadoTurno.CONFIRMADO);
        }

        @Test
        void confirmar_notOwner_denied() {
            when(turnoRepository.findById(100L)).thenReturn(Optional.of(turnoEnEstado(EstadoTurno.PENDIENTE)));

            assertThatThrownBy(() -> turnoService.confirmar("otro", 100L))
                    .isInstanceOf(AccessDeniedException.class);
        }

        @Test
        void confirmar_invalidState_throwsTransicion() {
            when(turnoRepository.findById(100L)).thenReturn(Optional.of(turnoEnEstado(EstadoTurno.CONFIRMADO)));

            assertThatThrownBy(() -> turnoService.confirmar("juan", 100L))
                    .isInstanceOf(TransicionInvalidaException.class);
        }

        @Test
        void confirmar_turnoNotFound_throws() {
            when(turnoRepository.findById(100L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> turnoService.confirmar("juan", 100L))
                    .isInstanceOf(ResourceNotFoundException.class);
        }

        @Test
        void confirmarPorToken_valido_movesToConfirmado() {
            when(turnoRepository.findByConfirmToken("tok-1"))
                    .thenReturn(Optional.of(turnoEnEstado(EstadoTurno.PENDIENTE)));
            when(turnoRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            assertThat(turnoService.confirmarPorToken("tok-1").getEstado())
                    .isEqualTo(EstadoTurno.CONFIRMADO);
        }

        @Test
        void confirmarPorToken_invalido_throwsNotFound() {
            when(turnoRepository.findByConfirmToken("bad")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> turnoService.confirmarPorToken("bad"))
                    .isInstanceOf(ResourceNotFoundException.class);
        }

        @Test
        void cancelar_owner_movesToCancelado() {
            when(turnoRepository.findById(100L)).thenReturn(Optional.of(turnoEnEstado(EstadoTurno.PENDIENTE)));
            when(turnoRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            assertThat(turnoService.cancelar("juan", 100L).getEstado()).isEqualTo(EstadoTurno.CANCELADO);
        }

        @Test
        void cancelar_assignedPeluquero_movesToCancelado() {
            when(turnoRepository.findById(100L)).thenReturn(Optional.of(turnoEnEstado(EstadoTurno.CONFIRMADO)));
            when(turnoRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            assertThat(turnoService.cancelar("marta", 100L).getEstado()).isEqualTo(EstadoTurno.CANCELADO);
        }

        @Test
        void cancelar_stranger_denied() {
            when(turnoRepository.findById(100L)).thenReturn(Optional.of(turnoEnEstado(EstadoTurno.PENDIENTE)));

            assertThatThrownBy(() -> turnoService.cancelar("otro", 100L))
                    .isInstanceOf(AccessDeniedException.class);
        }

        @Test
        void completar_assignedPeluquero_movesToCompletado() {
            when(turnoRepository.findById(100L)).thenReturn(Optional.of(turnoEnEstado(EstadoTurno.CONFIRMADO)));
            when(turnoRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            assertThat(turnoService.completar("marta", 100L).getEstado()).isEqualTo(EstadoTurno.COMPLETADO);
        }

        @Test
        void completar_notPeluquero_denied() {
            when(turnoRepository.findById(100L)).thenReturn(Optional.of(turnoEnEstado(EstadoTurno.CONFIRMADO)));

            assertThatThrownBy(() -> turnoService.completar("juan", 100L))
                    .isInstanceOf(AccessDeniedException.class);
        }
    }
}
