package be.parcial.controllers;

import be.parcial.dtos.TratamientoRequestDTO;
import be.parcial.dtos.TratamientoResponseDTO;
import be.parcial.services.TratamientoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tratamientos")
@RequiredArgsConstructor
@Tag(name = "Tratamientos", description = "Gestión de tratamientos de la peluquería")
public class TratamientoController {

    private final TratamientoService tratamientoService;

    @GetMapping
    @Operation(summary = "Lista los tratamientos activos")
    public ResponseEntity<List<TratamientoResponseDTO>> findAll() {
        return ResponseEntity.ok(tratamientoService.findAllActive());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtiene un tratamiento por id")
    public ResponseEntity<TratamientoResponseDTO> findById(@PathVariable Long id) {
        return ResponseEntity.ok(tratamientoService.findById(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Crea un tratamiento (solo ADMIN)")
    public ResponseEntity<TratamientoResponseDTO> create(
            @Valid @RequestBody TratamientoRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(tratamientoService.create(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Actualiza un tratamiento (solo ADMIN)")
    public ResponseEntity<TratamientoResponseDTO> update(
            @PathVariable Long id, @Valid @RequestBody TratamientoRequestDTO request) {
        return ResponseEntity.ok(tratamientoService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Baja lógica de un tratamiento (solo ADMIN)")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        tratamientoService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
