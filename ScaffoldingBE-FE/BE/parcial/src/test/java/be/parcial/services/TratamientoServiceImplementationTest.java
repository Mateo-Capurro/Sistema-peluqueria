package be.parcial.services;

import be.parcial.domain.entities.TratamientoEntity;
import be.parcial.dtos.TratamientoRequestDTO;
import be.parcial.dtos.TratamientoResponseDTO;
import be.parcial.exceptions.ResourceNotFoundException;
import be.parcial.repositories.TratamientoRepository;
import be.parcial.services.implementations.TratamientoServiceImplementation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TratamientoServiceImplementationTest {

    @Mock
    private TratamientoRepository tratamientoRepository;

    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private TratamientoServiceImplementation tratamientoService;

    private TratamientoEntity entity;
    private TratamientoRequestDTO request;
    private TratamientoResponseDTO responseDTO;

    @BeforeEach
    void setUp() {
        entity = new TratamientoEntity(1L, "Corte pelo corto", 30, new BigDecimal("4000.00"), true);
        request = new TratamientoRequestDTO("Corte pelo corto", 30, new BigDecimal("4000.00"));
        responseDTO = new TratamientoResponseDTO(1L, "Corte pelo corto", 30, new BigDecimal("4000.00"));
    }

    @Nested
    @DisplayName("create")
    class Create {

        @Test
        @DisplayName("should create tratamiento when nombre is unique")
        void create_uniqueNombre_returnsDto() {
            when(tratamientoRepository.existsByNombre("Corte pelo corto")).thenReturn(false);
            when(modelMapper.map(request, TratamientoEntity.class)).thenReturn(entity);
            when(tratamientoRepository.save(any(TratamientoEntity.class))).thenReturn(entity);
            when(modelMapper.map(entity, TratamientoResponseDTO.class)).thenReturn(responseDTO);

            TratamientoResponseDTO result = tratamientoService.create(request);

            assertThat(result).isEqualTo(responseDTO);
            assertThat(entity.isActivo()).isTrue();
            verify(tratamientoRepository).save(entity);
        }

        @Test
        @DisplayName("should throw when nombre already exists")
        void create_duplicateNombre_throws() {
            when(tratamientoRepository.existsByNombre("Corte pelo corto")).thenReturn(true);

            assertThatThrownBy(() -> tratamientoService.create(request))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Tratamiento already exists");
            verify(tratamientoRepository, never()).save(any());
        }
    }

    @Test
    @DisplayName("findAllActive maps active tratamientos")
    void findAllActive_returnsList() {
        when(tratamientoRepository.findByActivoTrue()).thenReturn(List.of(entity));
        when(modelMapper.map(entity, TratamientoResponseDTO.class)).thenReturn(responseDTO);

        List<TratamientoResponseDTO> result = tratamientoService.findAllActive();

        assertThat(result).containsExactly(responseDTO);
    }

    @Nested
    @DisplayName("findById")
    class FindById {

        @Test
        @DisplayName("should return dto when found")
        void findById_found_returnsDto() {
            when(tratamientoRepository.findById(1L)).thenReturn(Optional.of(entity));
            when(modelMapper.map(entity, TratamientoResponseDTO.class)).thenReturn(responseDTO);

            assertThat(tratamientoService.findById(1L)).isEqualTo(responseDTO);
        }

        @Test
        @DisplayName("should throw when not found")
        void findById_notFound_throws() {
            when(tratamientoRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> tratamientoService.findById(99L))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Tratamiento not found");
        }
    }

    @Test
    @DisplayName("update overwrites fields and saves")
    void update_found_updatesEntity() {
        TratamientoRequestDTO updateReq =
                new TratamientoRequestDTO("Corte pelo largo", 45, new BigDecimal("5500.00"));
        when(tratamientoRepository.findById(1L)).thenReturn(Optional.of(entity));
        when(tratamientoRepository.save(entity)).thenReturn(entity);
        when(modelMapper.map(eq(entity), eq(TratamientoResponseDTO.class))).thenReturn(responseDTO);

        tratamientoService.update(1L, updateReq);

        assertThat(entity.getNombre()).isEqualTo("Corte pelo largo");
        assertThat(entity.getDuracionMinutos()).isEqualTo(45);
        assertThat(entity.getPrecio()).isEqualByComparingTo("5500.00");
        verify(tratamientoRepository).save(entity);
    }

    @Test
    @DisplayName("delete performs logical removal (activo=false)")
    void delete_found_setsInactive() {
        when(tratamientoRepository.findById(1L)).thenReturn(Optional.of(entity));

        tratamientoService.delete(1L);

        assertThat(entity.isActivo()).isFalse();
        verify(tratamientoRepository).save(entity);
    }
}
