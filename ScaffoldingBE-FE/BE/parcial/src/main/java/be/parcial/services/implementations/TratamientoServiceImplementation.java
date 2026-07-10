package be.parcial.services.implementations;

import be.parcial.domain.entities.TratamientoEntity;
import be.parcial.dtos.TratamientoRequestDTO;
import be.parcial.dtos.TratamientoResponseDTO;
import be.parcial.exceptions.Messages;
import be.parcial.exceptions.ResourceNotFoundException;
import be.parcial.repositories.TratamientoRepository;
import be.parcial.services.TratamientoService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TratamientoServiceImplementation implements TratamientoService {

    private final TratamientoRepository tratamientoRepository;
    private final ModelMapper modelMapper;

    @Override
    public TratamientoResponseDTO create(TratamientoRequestDTO request) {
        if (tratamientoRepository.existsByNombre(request.getNombre())) {
            throw new IllegalArgumentException(
                    String.format(Messages.TRATAMIENTO_ALREADY_EXISTS, request.getNombre()));
        }
        TratamientoEntity entity = modelMapper.map(request, TratamientoEntity.class);
        entity.setId(null);
        entity.setActivo(true);
        TratamientoEntity saved = tratamientoRepository.save(entity);
        return modelMapper.map(saved, TratamientoResponseDTO.class);
    }

    @Override
    public List<TratamientoResponseDTO> findAllActive() {
        return tratamientoRepository.findByActivoTrue().stream()
                .map(entity -> modelMapper.map(entity, TratamientoResponseDTO.class))
                .toList();
    }

    @Override
    public TratamientoResponseDTO findById(Long id) {
        return modelMapper.map(getEntity(id), TratamientoResponseDTO.class);
    }

    @Override
    public TratamientoResponseDTO update(Long id, TratamientoRequestDTO request) {
        TratamientoEntity entity = getEntity(id);
        entity.setNombre(request.getNombre());
        entity.setDuracionMinutos(request.getDuracionMinutos());
        entity.setPrecio(request.getPrecio());
        TratamientoEntity saved = tratamientoRepository.save(entity);
        return modelMapper.map(saved, TratamientoResponseDTO.class);
    }

    @Override
    public void delete(Long id) {
        TratamientoEntity entity = getEntity(id);
        entity.setActivo(false);
        tratamientoRepository.save(entity);
    }

    private TratamientoEntity getEntity(Long id) {
        return tratamientoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        String.format(Messages.TRATAMIENTO_NOT_FOUND, id)));
    }
}
