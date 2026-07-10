package be.parcial.services;

import be.parcial.dtos.TratamientoRequestDTO;
import be.parcial.dtos.TratamientoResponseDTO;

import java.util.List;

public interface TratamientoService {
    TratamientoResponseDTO create(TratamientoRequestDTO request);
    List<TratamientoResponseDTO> findAllActive();
    TratamientoResponseDTO findById(Long id);
    TratamientoResponseDTO update(Long id, TratamientoRequestDTO request);
    void delete(Long id);
}
