package be.parcial.services.implementations;

import be.parcial.domain.entities.PeluqueroEntity;
import be.parcial.domain.entities.UserEntity;
import be.parcial.dtos.PeluqueroRequestDTO;
import be.parcial.dtos.PeluqueroResponseDTO;
import be.parcial.dtos.PeluqueroUpdateRequestDTO;
import be.parcial.dtos.RegisterRequestDTO;
import be.parcial.exceptions.Messages;
import be.parcial.exceptions.ResourceNotFoundException;
import be.parcial.repositories.PeluqueroRepository;
import be.parcial.services.PeluqueroService;
import be.parcial.services.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class PeluqueroServiceImplementation implements PeluqueroService {

    private final PeluqueroRepository peluqueroRepository;
    private final UserService userService;

    @Override
    public PeluqueroResponseDTO create(PeluqueroRequestDTO request) {
        validateJornada(request.getHoraInicio(), request.getHoraFin());

        RegisterRequestDTO userRequest = new RegisterRequestDTO(
                request.getUsername(),
                request.getPassword(),
                request.getName(),
                request.getEmail(),
                request.getTelefono(),
                request.getDni());
        UserEntity user = userService.registerUser(userRequest, UserEntity.Role.PELUQUERO);

        PeluqueroEntity peluquero = new PeluqueroEntity();
        peluquero.setUser(user);
        peluquero.setHoraInicio(request.getHoraInicio());
        peluquero.setHoraFin(request.getHoraFin());
        peluquero.setActivo(true);

        return toDto(peluqueroRepository.save(peluquero));
    }

    @Override
    public List<PeluqueroResponseDTO> findAllActive() {
        return peluqueroRepository.findByActivoTrue().stream()
                .map(this::toDto)
                .toList();
    }

    @Override
    public PeluqueroResponseDTO findById(Long id) {
        return toDto(getEntity(id));
    }

    @Override
    public PeluqueroResponseDTO update(Long id, PeluqueroUpdateRequestDTO request) {
        validateJornada(request.getHoraInicio(), request.getHoraFin());
        PeluqueroEntity peluquero = getEntity(id);
        peluquero.setHoraInicio(request.getHoraInicio());
        peluquero.setHoraFin(request.getHoraFin());
        peluquero.setActivo(request.isActivo());
        return toDto(peluqueroRepository.save(peluquero));
    }

    private void validateJornada(LocalTime horaInicio, LocalTime horaFin) {
        if (!horaInicio.isBefore(horaFin)) {
            throw new IllegalArgumentException(Messages.INVALID_JORNADA);
        }
    }

    private PeluqueroEntity getEntity(Long id) {
        return peluqueroRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        String.format(Messages.PELUQUERO_NOT_FOUND, id)));
    }

    private PeluqueroResponseDTO toDto(PeluqueroEntity peluquero) {
        return new PeluqueroResponseDTO(
                peluquero.getId(),
                peluquero.getUser().getName(),
                peluquero.getHoraInicio(),
                peluquero.getHoraFin(),
                peluquero.isActivo());
    }
}
