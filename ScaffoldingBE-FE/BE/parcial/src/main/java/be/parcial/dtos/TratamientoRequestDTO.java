package be.parcial.dtos;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TratamientoRequestDTO {

    @NotBlank(message = "Nombre cannot be blank")
    private String nombre;

    @Min(value = 1, message = "Duracion must be at least 1 minute")
    private int duracionMinutos;

    @NotNull(message = "Precio cannot be null")
    @DecimalMin(value = "0.0", message = "Precio must be zero or positive")
    private BigDecimal precio;
}
