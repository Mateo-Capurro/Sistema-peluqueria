package be.parcial.services;

import be.parcial.dtos.PeluqueroRequestDTO;
import be.parcial.dtos.PeluqueroResponseDTO;
import be.parcial.dtos.PeluqueroUpdateRequestDTO;

import java.util.List;

public interface PeluqueroService {
    PeluqueroResponseDTO create(PeluqueroRequestDTO request);
    List<PeluqueroResponseDTO> findAllActive();
    PeluqueroResponseDTO findById(Long id);
    PeluqueroResponseDTO update(Long id, PeluqueroUpdateRequestDTO request);
}
