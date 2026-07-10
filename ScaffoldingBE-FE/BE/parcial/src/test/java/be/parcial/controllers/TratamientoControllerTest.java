package be.parcial.controllers;

import be.parcial.dtos.TratamientoRequestDTO;
import be.parcial.dtos.TratamientoResponseDTO;
import be.parcial.exceptions.GlobalExceptionHandler;
import be.parcial.services.TratamientoService;
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

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

@ExtendWith(MockitoExtension.class)
class TratamientoControllerTest {

    private MockMvc mockMvc;

    @Mock
    private TratamientoService tratamientoService;

    @InjectMocks
    private TratamientoController tratamientoController;

    private JsonMapper jsonMapper;
    private TratamientoResponseDTO responseDTO;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(tratamientoController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        jsonMapper = JsonMapper.builder().build();
        responseDTO = new TratamientoResponseDTO(1L, "Corte pelo corto", 30, new BigDecimal("4000.00"));
    }

    @Test
    @DisplayName("GET /api/tratamientos returns 200 with list")
    void findAll_returns200() throws Exception {
        when(tratamientoService.findAllActive()).thenReturn(List.of(responseDTO));

        MockHttpServletResponse response = mockMvc.perform(get("/api/tratamientos"))
                .andReturn().getResponse();

        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(response.getContentAsString()).contains("Corte pelo corto");
    }

    @Test
    @DisplayName("GET /api/tratamientos/{id} returns 200")
    void findById_returns200() throws Exception {
        when(tratamientoService.findById(1L)).thenReturn(responseDTO);

        MockHttpServletResponse response = mockMvc.perform(get("/api/tratamientos/1"))
                .andReturn().getResponse();

        assertThat(response.getStatus()).isEqualTo(200);
        verify(tratamientoService).findById(1L);
    }

    @Test
    @DisplayName("POST /api/tratamientos returns 201")
    void create_returns201() throws Exception {
        TratamientoRequestDTO request =
                new TratamientoRequestDTO("Corte pelo corto", 30, new BigDecimal("4000.00"));
        when(tratamientoService.create(any(TratamientoRequestDTO.class))).thenReturn(responseDTO);

        MockHttpServletResponse response = mockMvc.perform(post("/api/tratamientos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(request)))
                .andReturn().getResponse();

        assertThat(response.getStatus()).isEqualTo(201);
        verify(tratamientoService).create(any(TratamientoRequestDTO.class));
    }

    @Test
    @DisplayName("PUT /api/tratamientos/{id} returns 200")
    void update_returns200() throws Exception {
        TratamientoRequestDTO request =
                new TratamientoRequestDTO("Corte pelo largo", 45, new BigDecimal("5500.00"));
        when(tratamientoService.update(eq(1L), any(TratamientoRequestDTO.class))).thenReturn(responseDTO);

        MockHttpServletResponse response = mockMvc.perform(put("/api/tratamientos/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(request)))
                .andReturn().getResponse();

        assertThat(response.getStatus()).isEqualTo(200);
        verify(tratamientoService).update(eq(1L), any(TratamientoRequestDTO.class));
    }

    @Test
    @DisplayName("DELETE /api/tratamientos/{id} returns 204")
    void delete_returns204() throws Exception {
        MockHttpServletResponse response = mockMvc.perform(delete("/api/tratamientos/1"))
                .andReturn().getResponse();

        assertThat(response.getStatus()).isEqualTo(204);
        verify(tratamientoService).delete(1L);
    }
}
