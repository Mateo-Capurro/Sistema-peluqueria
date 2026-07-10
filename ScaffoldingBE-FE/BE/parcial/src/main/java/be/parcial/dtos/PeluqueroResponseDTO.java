package be.parcial.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PeluqueroResponseDTO {

    private Long id;
    private String nombre;
    private LocalTime horaInicio;
    private LocalTime horaFin;
    private boolean activo;
}
