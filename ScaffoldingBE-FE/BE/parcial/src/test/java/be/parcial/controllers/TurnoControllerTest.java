package be.parcial.controllers;

import be.parcial.domain.entities.EstadoTurno;
import be.parcial.dtos.DisponibilidadResponseDTO;
import be.parcial.dtos.ReservaTurnoRequestDTO;
import be.parcial.dtos.TurnoResponseDTO;
import be.parcial.exceptions.GlobalExceptionHandler;
import be.parcial.services.DisponibilidadService;
import be.parcial.services.TurnoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import tools.jackson.databind.json.JsonMapper;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

@ExtendWith(MockitoExtension.class)
class TurnoControllerTest {

    private MockMvc mockMvc;

    @Mock private TurnoService turnoService;
    @Mock private DisponibilidadService disponibilidadService;

    @InjectMocks private TurnoController turnoController;

    private JsonMapper jsonMapper;
    private final Authentication authCliente =
            new UsernamePasswordAuthenticationToken("juan", null, List.of());
    private final Authentication authPeluquero =
            new UsernamePasswordAuthenticationToken("marta", null, List.of());
    private TurnoResponseDTO turnoDTO;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(turnoController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        jsonMapper = JsonMapper.builder().build();
        turnoDTO = new TurnoResponseDTO(1L, "Juan Perez", "Marta Rodriguez", "Corte",
                LocalDateTime.of(2026, 7, 14, 10, 0), LocalDateTime.of(2026, 7, 14, 10, 30),
                EstadoTurno.PENDIENTE);
    }

    @Test
    @DisplayName("GET /disponibilidad returns 200")
    void disponibilidad_returns200() throws Exception {
        DisponibilidadResponseDTO disp =
                new DisponibilidadResponseDTO(5L, LocalDate.of(2026, 7, 14), List.of());
        when(disponibilidadService.calcular(eq(5L), eq(LocalDate.of(2026, 7, 14)), eq(7L)))
                .thenReturn(disp);

        MockHttpServletResponse response = mockMvc.perform(get("/api/turnos/disponibilidad")
                        .param("peluqueroId", "5")
                        .param("fecha", "2026-07-14")
                        .param("tratamientoId", "7")
                        .principal(authCliente))
                .andReturn().getResponse();

        assertThat(response.getStatus()).isEqualTo(200);
        verify(disponibilidadService).calcular(5L, LocalDate.of(2026, 7, 14), 7L);
    }

    @Test
    @DisplayName("POST /api/turnos returns 201 and uses authenticated username")
    void reservar_returns201() throws Exception {
        ReservaTurnoRequestDTO request =
                new ReservaTurnoRequestDTO(5L, 7L, LocalDateTime.of(2030, 7, 14, 10, 0));
        when(turnoService.reservar(eq("juan"), any(ReservaTurnoRequestDTO.class))).thenReturn(turnoDTO);

        MockHttpServletResponse response = mockMvc.perform(post("/api/turnos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(request))
                        .principal(authCliente))
                .andReturn().getResponse();

        assertThat(response.getStatus()).isEqualTo(201);
        verify(turnoService).reservar(eq("juan"), any(ReservaTurnoRequestDTO.class));
    }

    @Test
    @DisplayName("GET /mios returns 200")
    void misTurnos_returns200() throws Exception {
        when(turnoService.misTurnos("juan")).thenReturn(List.of(turnoDTO));

        MockHttpServletResponse response = mockMvc.perform(get("/api/turnos/mios")
                        .principal(authCliente))
                .andReturn().getResponse();

        assertThat(response.getStatus()).isEqualTo(200);
        verify(turnoService).misTurnos("juan");
    }

    @Test
    @DisplayName("GET /agenda returns 200")
    void agenda_returns200() throws Exception {
        when(turnoService.agenda("marta")).thenReturn(List.of(turnoDTO));

        MockHttpServletResponse response = mockMvc.perform(get("/api/turnos/agenda")
                        .principal(authPeluquero))
                .andReturn().getResponse();

        assertThat(response.getStatus()).isEqualTo(200);
        verify(turnoService).agenda("marta");
    }

    @Test
    @DisplayName("PATCH /{id}/confirmar returns 200")
    void confirmar_returns200() throws Exception {
        when(turnoService.confirmar("juan", 1L)).thenReturn(turnoDTO);

        MockHttpServletResponse response = mockMvc.perform(patch("/api/turnos/1/confirmar")
                        .principal(authCliente))
                .andReturn().getResponse();

        assertThat(response.getStatus()).isEqualTo(200);
        verify(turnoService).confirmar("juan", 1L);
    }

    @Test
    @DisplayName("POST /confirmar/token/{token} returns 200")
    void confirmarPorToken_returns200() throws Exception {
        when(turnoService.confirmarPorToken("tok-1")).thenReturn(turnoDTO);

        MockHttpServletResponse response = mockMvc.perform(
                        post("/api/turnos/confirmar/token/tok-1"))
                .andReturn().getResponse();

        assertThat(response.getStatus()).isEqualTo(200);
        verify(turnoService).confirmarPorToken("tok-1");
    }

    @Test
    @DisplayName("PATCH /{id}/cancelar returns 200")
    void cancelar_returns200() throws Exception {
        when(turnoService.cancelar("juan", 1L)).thenReturn(turnoDTO);

        MockHttpServletResponse response = mockMvc.perform(patch("/api/turnos/1/cancelar")
                        .principal(authCliente))
                .andReturn().getResponse();

        assertThat(response.getStatus()).isEqualTo(200);
        verify(turnoService).cancelar("juan", 1L);
    }

    @Test
    @DisplayName("PATCH /{id}/completar returns 200")
    void completar_returns200() throws Exception {
        when(turnoService.completar("marta", 1L)).thenReturn(turnoDTO);

        MockHttpServletResponse response = mockMvc.perform(patch("/api/turnos/1/completar")
                        .principal(authPeluquero))
                .andReturn().getResponse();

        assertThat(response.getStatus()).isEqualTo(200);
        verify(turnoService).completar("marta", 1L);
    }
}
