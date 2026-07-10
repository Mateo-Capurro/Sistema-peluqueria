package be.parcial.services;

import be.parcial.domain.entities.PeluqueroEntity;
import be.parcial.domain.entities.UserEntity;
import be.parcial.dtos.PeluqueroRequestDTO;
import be.parcial.dtos.PeluqueroResponseDTO;
import be.parcial.dtos.PeluqueroUpdateRequestDTO;
import be.parcial.dtos.RegisterRequestDTO;
import be.parcial.exceptions.ResourceNotFoundException;
import be.parcial.repositories.PeluqueroRepository;
import be.parcial.services.implementations.PeluqueroServiceImplementation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PeluqueroServiceImplementationTest {

    @Mock
    private PeluqueroRepository peluqueroRepository;

    @Mock
    private UserService userService;

    @InjectMocks
    private PeluqueroServiceImplementation peluqueroService;

    private UserEntity user;
    private PeluqueroEntity peluquero;
    private PeluqueroRequestDTO request;

    @BeforeEach
    void setUp() {
        user = new UserEntity();
        user.setId(10L);
        user.setUsername("mrodriguez");
        user.setName("Marta Rodriguez");
        user.setRole(UserEntity.Role.PELUQUERO);

        peluquero = new PeluqueroEntity(1L, user, LocalTime.of(9, 0), LocalTime.of(18, 0), true);

        request = new PeluqueroRequestDTO("mrodriguez", "password123", "Marta Rodriguez",
                "mrodriguez@peluqueria.com", "1100000010", "30000010",
                LocalTime.of(9, 0), LocalTime.of(18, 0));
    }

    @Nested
    @DisplayName("create")
    class Create {

        @Test
        @DisplayName("should register PELUQUERO account and persist peluquero")
        void create_validJornada_returnsDto() {
            when(userService.registerUser(any(RegisterRequestDTO.class), eq(UserEntity.Role.PELUQUERO)))
                    .thenReturn(user);
            when(peluqueroRepository.save(any(PeluqueroEntity.class))).thenReturn(peluquero);

            PeluqueroResponseDTO result = peluqueroService.create(request);

            assertThat(result.getId()).isEqualTo(1L);
            assertThat(result.getNombre()).isEqualTo("Marta Rodriguez");
            assertThat(result.getHoraInicio()).isEqualTo(LocalTime.of(9, 0));
            assertThat(result.isActivo()).isTrue();
            verify(userService).registerUser(any(RegisterRequestDTO.class), eq(UserEntity.Role.PELUQUERO));
        }

        @Test
        @DisplayName("should throw when horaInicio is not before horaFin")
        void create_invalidJornada_throws() {
            request.setHoraInicio(LocalTime.of(18, 0));
            request.setHoraFin(LocalTime.of(9, 0));

            assertThatThrownBy(() -> peluqueroService.create(request))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("HoraInicio must be before HoraFin");
            verifyNoInteractions(userService);
            verify(peluqueroRepository, never()).save(any());
        }
    }

    @Test
    @DisplayName("findAllActive maps active peluqueros")
    void findAllActive_returnsList() {
        when(peluqueroRepository.findByActivoTrue()).thenReturn(List.of(peluquero));

        List<PeluqueroResponseDTO> result = peluqueroService.findAllActive();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getNombre()).isEqualTo("Marta Rodriguez");
    }

    @Nested
    @DisplayName("findById")
    class FindById {

        @Test
        @DisplayName("should return dto when found")
        void findById_found_returnsDto() {
            when(peluqueroRepository.findById(1L)).thenReturn(Optional.of(peluquero));

            assertThat(peluqueroService.findById(1L).getNombre()).isEqualTo("Marta Rodriguez");
        }

        @Test
        @DisplayName("should throw when not found")
        void findById_notFound_throws() {
            when(peluqueroRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> peluqueroService.findById(99L))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Peluquero not found");
        }
    }

    @Nested
    @DisplayName("update")
    class Update {

        @Test
        @DisplayName("should update jornada and estado")
        void update_valid_updatesEntity() {
            PeluqueroUpdateRequestDTO updateReq =
                    new PeluqueroUpdateRequestDTO(LocalTime.of(8, 0), LocalTime.of(16, 0), false);
            when(peluqueroRepository.findById(1L)).thenReturn(Optional.of(peluquero));
            when(peluqueroRepository.save(peluquero)).thenReturn(peluquero);

            PeluqueroResponseDTO result = peluqueroService.update(1L, updateReq);

            assertThat(peluquero.getHoraInicio()).isEqualTo(LocalTime.of(8, 0));
            assertThat(peluquero.getHoraFin()).isEqualTo(LocalTime.of(16, 0));
            assertThat(result.isActivo()).isFalse();
        }

        @Test
        @DisplayName("should throw on invalid jornada")
        void update_invalidJornada_throws() {
            PeluqueroUpdateRequestDTO updateReq =
                    new PeluqueroUpdateRequestDTO(LocalTime.of(16, 0), LocalTime.of(8, 0), true);

            assertThatThrownBy(() -> peluqueroService.update(1L, updateReq))
                    .isInstanceOf(IllegalArgumentException.class);
            verify(peluqueroRepository, never()).save(any());
        }
    }
}
