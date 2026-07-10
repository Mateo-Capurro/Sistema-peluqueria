package be.parcial.dtos;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReservaTurnoRequestDTO {

    @NotNull(message = "PeluqueroId cannot be null")
    private Long peluqueroId;

    @NotNull(message = "TratamientoId cannot be null")
    private Long tratamientoId;

    @NotNull(message = "Inicio cannot be null")
    @Future(message = "Inicio must be in the future")
    private LocalDateTime inicio;
}
