package be.parcial.controllers;

import be.parcial.dtos.PeluqueroRequestDTO;
import be.parcial.dtos.PeluqueroResponseDTO;
import be.parcial.dtos.PeluqueroUpdateRequestDTO;
import be.parcial.services.PeluqueroService;
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
@RequestMapping("/api/peluqueros")
@RequiredArgsConstructor
@Tag(name = "Peluqueros", description = "Gestión de peluqueros de la peluquería")
public class PeluqueroController {

    private final PeluqueroService peluqueroService;

    @GetMapping
    @Operation(summary = "Lista los peluqueros activos")
    public ResponseEntity<List<PeluqueroResponseDTO>> findAll() {
        return ResponseEntity.ok(peluqueroService.findAllActive());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtiene un peluquero por id")
    public ResponseEntity<PeluqueroResponseDTO> findById(@PathVariable Long id) {
        return ResponseEntity.ok(peluqueroService.findById(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Crea una cuenta de peluquero con su jornada (solo ADMIN)")
    public ResponseEntity<PeluqueroResponseDTO> create(
            @Valid @RequestBody PeluqueroRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(peluqueroService.create(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Actualiza jornada y estado de un peluquero (solo ADMIN)")
    public ResponseEntity<PeluqueroResponseDTO> update(
            @PathVariable Long id, @Valid @RequestBody PeluqueroUpdateRequestDTO request) {
        return ResponseEntity.ok(peluqueroService.update(id, request));
    }
}
