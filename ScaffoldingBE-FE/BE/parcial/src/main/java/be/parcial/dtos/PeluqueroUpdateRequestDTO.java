package be.parcial.dtos;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PeluqueroUpdateRequestDTO {

    @NotNull(message = "HoraInicio cannot be null")
    private LocalTime horaInicio;

    @NotNull(message = "HoraFin cannot be null")
    private LocalTime horaFin;

    private boolean activo;
}
