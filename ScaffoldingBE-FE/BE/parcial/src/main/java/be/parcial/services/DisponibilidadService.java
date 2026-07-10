package be.parcial.services;

import be.parcial.dtos.DisponibilidadResponseDTO;

import java.time.LocalDate;

public interface DisponibilidadService {
    DisponibilidadResponseDTO calcular(Long peluqueroId, LocalDate fecha, Long tratamientoId);
}
