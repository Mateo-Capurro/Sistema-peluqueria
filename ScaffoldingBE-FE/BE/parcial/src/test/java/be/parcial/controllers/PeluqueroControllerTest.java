package be.parcial.controllers;

import be.parcial.dtos.PeluqueroRequestDTO;
import be.parcial.dtos.PeluqueroResponseDTO;
import be.parcial.dtos.PeluqueroUpdateRequestDTO;
import be.parcial.exceptions.GlobalExceptionHandler;
import be.parcial.services.PeluqueroService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import tools.jackson.databind.json.JsonMapper;

import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

@ExtendWith(MockitoExtension.class)
class PeluqueroControllerTest {

    private MockMvc mockMvc;

    @Mock
    private PeluqueroService peluqueroService;

    @InjectMocks
    private PeluqueroController peluqueroController;

    private JsonMapper jsonMapper;
    private PeluqueroResponseDTO responseDTO;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(peluqueroController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        jsonMapper = JsonMapper.builder().build();
        responseDTO = new PeluqueroResponseDTO(
                1L, "Marta Rodriguez", LocalTime.of(9, 0), LocalTime.of(18, 0), true);
    }

    @Test
    @DisplayName("GET /api/peluqueros returns 200 with list")
    void findAll_returns200() throws Exception {
        when(peluqueroService.findAllActive()).thenReturn(List.of(responseDTO));

        MockHttpServletResponse response = mockMvc.perform(get("/api/peluqueros"))
                .andReturn().getResponse();

        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(response.getContentAsString()).contains("Marta Rodriguez");
    }

    @Test
    @DisplayName("GET /api/peluqueros/{id} returns 200")
    void findById_returns200() throws Exception {
        when(peluqueroService.findById(1L)).thenReturn(responseDTO);

        MockHttpServletResponse response = mockMvc.perform(get("/api/peluqueros/1"))
                .andReturn().getResponse();

        assertThat(response.getStatus()).isEqualTo(200);
        verify(peluqueroService).findById(1L);
    }

    @Test
    @DisplayName("POST /api/peluqueros returns 201")
    void create_returns201() throws Exception {
        PeluqueroRequestDTO request = new PeluqueroRequestDTO(
                "mrodriguez", "password123", "Marta Rodriguez",
                "mrodriguez@peluqueria.com", "1100000010", "30000010",
                LocalTime.of(9, 0), LocalTime.of(18, 0));
        when(peluqueroService.create(any(PeluqueroRequestDTO.class))).thenReturn(responseDTO);

        MockHttpServletResponse response = mockMvc.perform(post("/api/peluqueros")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(request)))
                .andReturn().getResponse();

        assertThat(response.getStatus()).isEqualTo(201);
        verify(peluqueroService).create(any(PeluqueroRequestDTO.class));
    }

    @Test
    @DisplayName("PUT /api/peluqueros/{id} returns 200")
    void update_returns200() throws Exception {
        PeluqueroUpdateRequestDTO request =
                new PeluqueroUpdateRequestDTO(LocalTime.of(8, 0), LocalTime.of(16, 0), false);
        when(peluqueroService.update(eq(1L), any(PeluqueroUpdateRequestDTO.class)))
                .thenReturn(responseDTO);

        MockHttpServletResponse response = mockMvc.perform(put("/api/peluqueros/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(request)))
                .andReturn().getResponse();

        assertThat(response.getStatus()).isEqualTo(200);
        verify(peluqueroService).update(eq(1L), any(PeluqueroUpdateRequestDTO.class));
    }
}
