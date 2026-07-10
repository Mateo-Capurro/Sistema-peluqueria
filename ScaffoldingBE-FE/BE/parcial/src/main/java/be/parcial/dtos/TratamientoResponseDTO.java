package be.parcial.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TratamientoResponseDTO {

    private Long id;
    private String nombre;
    private int duracionMinutos;
    private BigDecimal precio;
}
