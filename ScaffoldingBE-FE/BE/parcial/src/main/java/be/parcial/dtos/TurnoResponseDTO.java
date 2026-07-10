package be.parcial.dtos;

import be.parcial.domain.entities.EstadoTurno;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TurnoResponseDTO {

    private Long id;
    private String clienteNombre;
    private String peluqueroNombre;
    private String tratamientoNombre;
    private LocalDateTime inicio;
    private LocalDateTime fin;
    private EstadoTurno estado;
}
