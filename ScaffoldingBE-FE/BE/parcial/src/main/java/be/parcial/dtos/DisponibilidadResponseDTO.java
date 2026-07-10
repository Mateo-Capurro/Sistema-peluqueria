package be.parcial.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DisponibilidadResponseDTO {
    private Long peluqueroId;
    private LocalDate fecha;
    private List<SlotDTO> slots;
}
