package be.parcial.services;

import be.parcial.domain.entities.PeluqueroEntity;
import be.parcial.domain.entities.TratamientoEntity;
import be.parcial.domain.entities.TurnoEntity;
import be.parcial.domain.entities.UserEntity;
import be.parcial.dtos.DisponibilidadResponseDTO;
import be.parcial.dtos.SlotDTO;
import be.parcial.exceptions.ResourceNotFoundException;
import be.parcial.repositories.PeluqueroRepository;
import be.parcial.repositories.TratamientoRepository;
import be.parcial.repositories.TurnoRepository;
import be.parcial.services.disponibilidad.SlotCalculationStrategy;
import be.parcial.services.implementations.DisponibilidadServiceImplementation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DisponibilidadServiceImplementationTest {

    @Mock
    private PeluqueroRepository peluqueroRepository;
    @Mock
    private TratamientoRepository tratamientoRepository;
    @Mock
    private TurnoRepository turnoRepository;
    @Mock
    private SlotCalculationStrategy slotStrategy;

    @InjectMocks
    private DisponibilidadServiceImplementation disponibilidadService;

    private PeluqueroEntity peluquero;
    private TratamientoEntity tratamiento;
    private final LocalDate martes = LocalDate.of(2026, 7, 14);
    private final LocalDate lunes = LocalDate.of(2026, 7, 13);

    @BeforeEach
    void setUp() {
        UserEntity user = new UserEntity();
        user.setName("Marta Rodriguez");
        peluquero = new PeluqueroEntity(1L, user, LocalTime.of(9, 0), LocalTime.of(18, 0), true);
        tratamiento = new TratamientoEntity(2L, "Corte", 30, new BigDecimal("4000.00"), true);
    }

    @Test
    @DisplayName("valid day delegates to strategy and returns slots")
    void calcular_validDay_returnsSlots() {
        SlotDTO slot = new SlotDTO(
                LocalDateTime.of(martes, LocalTime.of(9, 0)),
                LocalDateTime.of(martes, LocalTime.of(9, 30)));
        TurnoEntity ocupado = new TurnoEntity();
        ocupado.setInicio(LocalDateTime.of(martes, LocalTime.of(10, 0)));
        ocupado.setFin(LocalDateTime.of(martes, LocalTime.of(10, 30)));

        when(peluqueroRepository.findById(1L)).thenReturn(Optional.of(peluquero));
        when(tratamientoRepository.findById(2L)).thenReturn(Optional.of(tratamiento));
        when(turnoRepository.findByPeluqueroIdAndEstadoInAndInicioBetween(
                eq(1L), anyCollection(), any(), any())).thenReturn(List.of(ocupado));
        when(slotStrategy.calcular(eq(martes), eq(LocalTime.of(9, 0)), eq(LocalTime.of(18, 0)),
                eq(30), anyList(), any())).thenReturn(List.of(slot));

        DisponibilidadResponseDTO result = disponibilidadService.calcular(1L, martes, 2L);

        assertThat(result.getPeluqueroId()).isEqualTo(1L);
        assertThat(result.getFecha()).isEqualTo(martes);
        assertThat(result.getSlots()).containsExactly(slot);
        verify(slotStrategy).calcular(eq(martes), any(), any(), eq(30), anyList(), any());
    }

    @Test
    @DisplayName("monday returns empty without calling strategy")
    void calcular_monday_returnsEmpty() {
        when(peluqueroRepository.findById(1L)).thenReturn(Optional.of(peluquero));
        when(tratamientoRepository.findById(2L)).thenReturn(Optional.of(tratamiento));

        DisponibilidadResponseDTO result = disponibilidadService.calcular(1L, lunes, 2L);

        assertThat(result.getSlots()).isEmpty();
        verifyNoInteractions(slotStrategy);
        verify(turnoRepository, never()).findByPeluqueroIdAndEstadoInAndInicioBetween(any(), any(), any(), any());
    }

    @Test
    @DisplayName("throws when peluquero not found")
    void calcular_peluqueroNotFound_throws() {
        when(peluqueroRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> disponibilidadService.calcular(1L, martes, 2L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Peluquero not found");
    }

    @Test
    @DisplayName("throws when tratamiento not found")
    void calcular_tratamientoNotFound_throws() {
        when(peluqueroRepository.findById(1L)).thenReturn(Optional.of(peluquero));
        when(tratamientoRepository.findById(2L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> disponibilidadService.calcular(1L, martes, 2L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Tratamiento not found");
    }
}
