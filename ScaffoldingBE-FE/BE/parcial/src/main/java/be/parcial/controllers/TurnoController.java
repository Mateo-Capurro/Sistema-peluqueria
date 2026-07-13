package be.parcial.controllers;

import be.parcial.dtos.DisponibilidadResponseDTO;
import be.parcial.dtos.ReservaTurnoRequestDTO;
import be.parcial.dtos.TurnoResponseDTO;
import be.parcial.services.DisponibilidadService;
import be.parcial.services.TurnoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/turnos")
@RequiredArgsConstructor
@Tag(name = "Turnos", description = "Reserva y gestión de turnos")
public class TurnoController {

    private final TurnoService turnoService;
    private final DisponibilidadService disponibilidadService;

    @GetMapping("/disponibilidad")
    @PreAuthorize("hasRole('CLIENTE')")
    @Operation(summary = "Horarios libres de un peluquero para una fecha y tratamiento")
    public ResponseEntity<DisponibilidadResponseDTO> disponibilidad(
            @RequestParam Long peluqueroId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha,
            @RequestParam Long tratamientoId) {
        return ResponseEntity.ok(
                disponibilidadService.calcular(peluqueroId, fecha, tratamientoId));
    }

    @PostMapping
    @PreAuthorize("hasRole('CLIENTE')")
    @Operation(summary = "Reserva un turno (queda PENDIENTE y dispara la notificación)")
    public ResponseEntity<TurnoResponseDTO> reservar(
            @Valid @RequestBody ReservaTurnoRequestDTO request, Authentication authentication) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(turnoService.reservar(authentication.getName(), request));
    }

    @GetMapping("/mios")
    @PreAuthorize("hasRole('CLIENTE')")
    @Operation(summary = "Turnos del cliente autenticado")
    public ResponseEntity<List<TurnoResponseDTO>> misTurnos(Authentication authentication) {
        return ResponseEntity.ok(turnoService.misTurnos(authentication.getName()));
    }

    @GetMapping("/agenda")
    @PreAuthorize("hasRole('PELUQUERO')")
    @Operation(summary = "Agenda del peluquero autenticado")
    public ResponseEntity<List<TurnoResponseDTO>> agenda(Authentication authentication) {
        return ResponseEntity.ok(turnoService.agenda(authentication.getName()));
    }

    @PatchMapping("/{id}/confirmar")
    @PreAuthorize("hasRole('CLIENTE')")
    @Operation(summary = "El cliente confirma su turno")
    public ResponseEntity<TurnoResponseDTO> confirmar(
            @PathVariable Long id, Authentication authentication) {
        return ResponseEntity.ok(turnoService.confirmar(authentication.getName(), id));
    }

    @PostMapping("/confirmar/token/{token}")
    @Operation(summary = "Confirma un turno vía token del email (público, sin login)")
    public ResponseEntity<TurnoResponseDTO> confirmarPorToken(@PathVariable String token) {
        return ResponseEntity.ok(turnoService.confirmarPorToken(token));
    }

    @PatchMapping("/{id}/cancelar")
    @PreAuthorize("hasAnyRole('CLIENTE','PELUQUERO')")
    @Operation(summary = "Cancela el turno (cliente dueño o peluquero asignado)")
    public ResponseEntity<TurnoResponseDTO> cancelar(
            @PathVariable Long id, Authentication authentication) {
        return ResponseEntity.ok(turnoService.cancelar(authentication.getName(), id));
    }

    @PatchMapping("/{id}/completar")
    @PreAuthorize("hasRole('PELUQUERO')")
    @Operation(summary = "El peluquero marca el turno como completado")
    public ResponseEntity<TurnoResponseDTO> completar(
            @PathVariable Long id, Authentication authentication) {
        return ResponseEntity.ok(turnoService.completar(authentication.getName(), id));
    }
}
