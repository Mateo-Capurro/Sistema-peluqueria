package be.parcial.services;

import be.parcial.dtos.ReservaTurnoRequestDTO;
import be.parcial.dtos.TurnoResponseDTO;

import java.util.List;

public interface TurnoService {
    TurnoResponseDTO reservar(String clienteUsername, ReservaTurnoRequestDTO request);
    List<TurnoResponseDTO> misTurnos(String clienteUsername);
    List<TurnoResponseDTO> agenda(String peluqueroUsername);
    TurnoResponseDTO confirmar(String username, Long turnoId);
    TurnoResponseDTO confirmarPorToken(String token);
    TurnoResponseDTO cancelar(String username, Long turnoId);
    TurnoResponseDTO completar(String username, Long turnoId);
}
