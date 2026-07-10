# SDD - Backend (Dominio: Sistema de Turnos de Peluquería)

> Documento de diseño del **dominio** de la peluquería. Extiende el scaffold de autenticación descrito en `SDD.md` (base inamovible: Java 21, Spring Boot 4.1.0, Maven, JWT, Spring Data JPA + H2, ModelMapper, JaCoCo 95% LINE/BRANCH). Aquí solo se especifica lo **nuevo** que se agrega sobre esa base.
>
> Basado en `../ANALYSIS.md`. Decisiones cerradas: notificación simulada tras interfaz, peluquero como actor con login, slots calculados por duración de tratamiento.

---

## 1. Objetivo del Backend (Dominio)

Extender la API REST del scaffold con la lógica de negocio de una peluquería: catálogo de peluqueros y tratamientos, cálculo de disponibilidad, reserva de turnos sin solapamiento, transiciones de estado y notificación al cliente. Toda la seguridad (JWT, filtro, hashing) se reutiliza del scaffold.

---

## 2. Cambios sobre el Scaffold Base

| Área | Cambio | Motivo |
| :--- | :--- | :--- |
| `UserEntity.Role` | Ampliar enum: `ADMIN, CLIENTE, PELUQUERO` (reemplaza `USER`). | Tres actores con permisos distintos. |
| `UserEntity` | Agregar `email` (unique), `telefono`, `dni` (unique). | Datos que exige la consigna. |
| `RegisterRequestDTO` | Agregar `email`, `telefono`, `dni`; `name` = nombre completo. | Registro de cliente completo. |
| `CustomUserDetailsService` | Mapear los nuevos roles a `ROLE_CLIENTE` / `ROLE_PELUQUERO` / `ROLE_ADMIN`. | Autorización por rol. |
| `AuthController` `/register` | Registrar siempre como `CLIENTE`. Peluqueros los crea el admin. | Ownership y separación de actores. |
| `data.sql` | Semillas: admin, peluqueros (con su `Peluquero`), tratamientos, cliente demo. | Datos reales para evaluar. |
| `SecurityConfig` | `/api/tratamientos` y `/api/peluqueros` (GET) autenticados; escritura solo `ADMIN`; turnos según rol vía `@PreAuthorize`. | Endpoints nuevos protegidos. |

> **Nota de compatibilidad:** al reemplazar `USER` por `CLIENTE` hay que actualizar los tests existentes que referencian `Role.USER` y `ROLE_USER`. Alternativa mínima: conservar `USER` y añadir `CLIENTE`, `PELUQUERO`; se decide **reemplazar** para un dominio limpio.

---

## 3. Estructura de Paquetes (Nuevos Artefactos)

Respeta la estructura obligatoria del scaffold (`be.parcial.*`):

```
domain/entities/     → PeluqueroEntity, TratamientoEntity, TurnoEntity  (+ enum EstadoTurno)
domain/models/       → Peluquero, Tratamiento, Turno  (modelos ModelMapper)
dtos/                → PeluqueroResponseDTO, TratamientoResponseDTO, TratamientoRequestDTO,
                       ReservaTurnoRequestDTO, TurnoResponseDTO, DisponibilidadResponseDTO, SlotDTO
repositories/        → PeluqueroRepository, TratamientoRepository, TurnoRepository
services/            → PeluqueroService, TratamientoService, TurnoService, DisponibilidadService,
                       NotificationService, NotificationChannel
services/implementations/ → *ServiceImplementation, EmailNotificationChannel,
                       WhatsappNotificationChannel, CompositeNotificationService
services/disponibilidad/  → SlotCalculationStrategy (Strategy) + impl
domain/estado/       → EstadoTurnoState (State) + PendienteState, ConfirmadoState, ...
controllers/         → PeluqueroController, TratamientoController, TurnoController
exceptions/          → SlotNoDisponibleException, TransicionInvalidaException (nuevas)
```

---

## 4. Modelo de Datos (Entidades JPA Nuevas)

### 4.1. `UserEntity` (Tabla `users`) — campos agregados
| Campo | Tipo | Anotaciones | Restricciones |
| :--- | :--- | :--- | :--- |
| `email` | `String` | `@Column(nullable=false, unique=true, length=150)` | Formato email. Único. |
| `telefono` | `String` | `@Column(nullable=false, length=30)` | Para WhatsApp. |
| `dni` | `String` | `@Column(nullable=false, unique=true, length=20)` | Único. |

### 4.2. `PeluqueroEntity` (Tabla `peluqueros`)
| Campo | Tipo | Anotaciones | Restricciones |
| :--- | :--- | :--- | :--- |
| `id` | `Long` | `@Id @GeneratedValue(IDENTITY)` | Autoincremental. |
| `user` | `UserEntity` | `@OneToOne(fetch=LAZY)` `@JoinColumn(name="user_id", unique=true)` | Cuenta con rol `PELUQUERO`. |
| `horaInicio` | `LocalTime` | `@Column(nullable=false)` | Inicio de jornada. |
| `horaFin` | `LocalTime` | `@Column(nullable=false)` | Fin de jornada. |
| `activo` | `boolean` | `@Column(nullable=false)` | Si acepta reservas. |

### 4.3. `TratamientoEntity` (Tabla `tratamientos`)
| Campo | Tipo | Anotaciones | Restricciones |
| :--- | :--- | :--- | :--- |
| `id` | `Long` | `@Id @GeneratedValue(IDENTITY)` | Autoincremental. |
| `nombre` | `String` | `@Column(nullable=false, unique=true, length=100)` | Ej. "Corte pelo largo". |
| `duracionMinutos` | `int` | `@Column(nullable=false)` | > 0. Define ocupación del slot. |
| `precio` | `BigDecimal` | `@Column(nullable=false, precision=10, scale=2)` | ≥ 0. |
| `activo` | `boolean` | `@Column(nullable=false)` | Si es reservable. |

### 4.4. `TurnoEntity` (Tabla `turnos`)
| Campo | Tipo | Anotaciones | Restricciones |
| :--- | :--- | :--- | :--- |
| `id` | `Long` | `@Id @GeneratedValue(IDENTITY)` | Autoincremental. |
| `cliente` | `UserEntity` | `@ManyToOne(fetch=LAZY)` `@JoinColumn(name="cliente_id")` | Rol `CLIENTE`. |
| `peluquero` | `PeluqueroEntity` | `@ManyToOne(fetch=LAZY)` `@JoinColumn(name="peluquero_id")` | — |
| `tratamiento` | `TratamientoEntity` | `@ManyToOne(fetch=LAZY)` `@JoinColumn(name="tratamiento_id")` | — |
| `inicio` | `LocalDateTime` | `@Column(nullable=false)` | Inicio del turno. |
| `fin` | `LocalDateTime` | `@Column(nullable=false)` | `inicio + duracionMinutos`. |
| `estado` | `EstadoTurno` | `@Enumerated(STRING) @Column(nullable=false)` | Ver 4.5. |
| `createdAt` | `LocalDateTime` | `@PrePersist` | — |
| `updatedAt` | `LocalDateTime` | `@PrePersist/@PreUpdate` | — |

### 4.5. Enum `EstadoTurno`
`PENDIENTE`, `CONFIRMADO`, `CANCELADO`, `COMPLETADO`.

Transiciones válidas:
```
PENDIENTE  → CONFIRMADO | CANCELADO
CONFIRMADO → COMPLETADO | CANCELADO
CANCELADO  → (terminal)
COMPLETADO → (terminal)
```

---

## 5. Reglas de Negocio (Servicios)

### 5.1. Reserva (`TurnoService.reservar`)
1. Cliente autenticado; peluquero y tratamiento existen y están activos.
2. `inicio` en el futuro.
3. Día de `inicio` ∈ {MARTES..DOMINGO} (rechaza LUNES).
4. `fin = inicio + tratamiento.duracionMinutos`.
5. `[inicio, fin)` dentro de `[peluquero.horaInicio, peluquero.horaFin)`.
6. No hay solapamiento con otro turno del mismo peluquero en estado `PENDIENTE`/`CONFIRMADO`.
7. Crea `TurnoEntity` en `PENDIENTE` → persiste → dispara `NotificationService.notifyReserva(turno)`.

**Solapamiento (repositorio):**
```java
// existe turno activo del peluquero que se cruza con [inicio, fin)
boolean existsByPeluqueroIdAndEstadoInAndInicioLessThanAndFinGreaterThan(
    Long peluqueroId, Collection<EstadoTurno> activos, LocalDateTime fin, LocalDateTime inicio);
```

### 5.2. Disponibilidad (`DisponibilidadService.calcular`)
Entrada: `peluqueroId`, `fecha` (LocalDate), `tratamientoId`. Salida: lista de `SlotDTO { inicio, fin }` libres.
- Genera candidatos desde `horaInicio` avanzando en pasos (paso configurable, def. 15 min) mientras `candidato + duracion ≤ horaFin`.
- Descarta candidatos que se solapan con turnos activos existentes o que ya pasaron.
- Si `fecha` es LUNES o pasada → lista vacía.

### 5.3. Transiciones (`TurnoService.confirmar/cancelar/completar`)
- `confirmar`: solo el **cliente dueño**; `PENDIENTE → CONFIRMADO`.
- `cancelar`: cliente dueño **o** peluquero asignado; `PENDIENTE|CONFIRMADO → CANCELADO`.
- `completar`: solo peluquero asignado; `CONFIRMADO → COMPLETADO`.
- Transición inválida → `TransicionInvalidaException` (400).
- Falta de ownership → `403` (acceso denegado).

### 5.4. Notificación (`NotificationService`)
`notifyReserva(TurnoEntity)` recorre los `NotificationChannel` registrados y envía. Implementaciones simuladas registran el envío (log). Enchufable a proveedor real sin tocar `TurnoService`.

---

## 6. Patrones de Diseño

| Patrón | Uso | Artefacto |
| :--- | :--- | :--- |
| **Strategy** | Cálculo de slots libres (algoritmo intercambiable: paso fijo vs. otros). | `SlotCalculationStrategy` + `FixedStepSlotStrategy`. |
| **State** | Transiciones de `EstadoTurno` con reglas por estado. | `EstadoTurnoState` + estados concretos; valida transición y ejecuta efecto. |
| **Composite** | Enviar por todos los canales como si fuera uno. | `CompositeNotificationService implements NotificationService`. |
| **Registry / DI** | Descubrir canales activos vía inyección de `List<NotificationChannel>`. | Spring inyecta todos los beans `NotificationChannel`. |

---

## 7. Contratos de API (Endpoints Nuevos)

Todos requieren `Authorization: Bearer {token}` salvo indicación. Errores en formato `ErrorResponse`.

### 7.1. Tratamientos — `/api/tratamientos`
| Método | Endpoint | Rol | Descripción |
| :--- | :--- | :--- | :--- |
| GET | `/api/tratamientos` | autenticado | Lista tratamientos activos. |
| GET | `/api/tratamientos/{id}` | autenticado | Detalle. |
| POST | `/api/tratamientos` | ADMIN | Crea tratamiento. |
| PUT | `/api/tratamientos/{id}` | ADMIN | Actualiza. |
| DELETE | `/api/tratamientos/{id}` | ADMIN | Baja lógica (`activo=false`). |

### 7.2. Peluqueros — `/api/peluqueros`
| Método | Endpoint | Rol | Descripción |
| :--- | :--- | :--- | :--- |
| GET | `/api/peluqueros` | autenticado | Lista peluqueros activos. |
| GET | `/api/peluqueros/{id}` | autenticado | Detalle. |
| POST | `/api/peluqueros` | ADMIN | Crea cuenta PELUQUERO + jornada. |
| PUT | `/api/peluqueros/{id}` | ADMIN | Actualiza jornada/estado. |

### 7.3. Turnos — `/api/turnos`
| Método | Endpoint | Rol | Descripción |
| :--- | :--- | :--- | :--- |
| GET | `/api/turnos/disponibilidad?peluqueroId&fecha&tratamientoId` | CLIENTE | Slots libres. |
| POST | `/api/turnos` | CLIENTE | Reserva (crea PENDIENTE + notifica). |
| GET | `/api/turnos/mios` | CLIENTE | Turnos del cliente autenticado. |
| GET | `/api/turnos/agenda` | PELUQUERO | Turnos del peluquero autenticado. |
| PATCH | `/api/turnos/{id}/confirmar` | CLIENTE (dueño) | PENDIENTE→CONFIRMADO. |
| PATCH | `/api/turnos/{id}/cancelar` | CLIENTE dueño / PELUQUERO asignado | →CANCELADO. |
| PATCH | `/api/turnos/{id}/completar` | PELUQUERO asignado | CONFIRMADO→COMPLETADO. |

---

## 8. DTOs Nuevos

**Requests:**
- `RegisterRequestDTO` (ampliado): `{ username, password, name, email, telefono, dni }` — `@Email`, `@NotBlank` en dni/telefono.
- `TratamientoRequestDTO`: `{ nombre, duracionMinutos(>0), precio(≥0) }`.
- `PeluqueroRequestDTO`: `{ username, password, name, email, telefono, dni, horaInicio, horaFin }`.
- `ReservaTurnoRequestDTO`: `{ peluqueroId, tratamientoId, inicio (LocalDateTime, @Future) }`.

**Responses (nunca exponen entidad ni password):**
- `TratamientoResponseDTO`: `{ id, nombre, duracionMinutos, precio }`.
- `PeluqueroResponseDTO`: `{ id, nombre, horaInicio, horaFin, activo }`.
- `TurnoResponseDTO`: `{ id, clienteNombre, peluqueroNombre, tratamientoNombre, inicio, fin, estado }`.
- `SlotDTO`: `{ inicio, fin }`.
- `DisponibilidadResponseDTO`: `{ peluqueroId, fecha, slots: SlotDTO[] }`.

---

## 9. Excepciones y Manejo de Errores (agregados a `GlobalExceptionHandler`)

| Excepción | Status | Cuándo |
| :--- | :--- | :--- |
| `SlotNoDisponibleException` | `409 Conflict` | Solapamiento / fuera de jornada / día no laborable. |
| `TransicionInvalidaException` | `400 Bad Request` | Cambio de estado no permitido. |
| `AccessDeniedException` | `403 Forbidden` | Ownership: turno de otro usuario. |
| `ResourceNotFoundException` (existe) | `404` | Peluquero/tratamiento/turno inexistente. |

Todas devuelven `ErrorResponse { status, error, message, path, timestamp, details }`.

---

## 10. Seguridad y Ownership

- Reutiliza JWT del scaffold. Autorización por rol con `@PreAuthorize("hasRole('CLIENTE')")` etc. en controllers (ya está `@EnableMethodSecurity`).
- Ownership verificado en la capa de servicio comparando el `username` autenticado (`SecurityContextHolder`) contra el dueño del turno; nunca confiar en IDs del request.
- `PeluqueroResponseDTO`/`TurnoResponseDTO` exponen solo nombres, nunca `UserEntity` completa ni password.

---

## 11. Semillas (`data.sql`)

- 1 admin (existe).
- 2 peluqueros: cuenta rol PELUQUERO + fila `peluqueros` con jornada (ej. 09:00–18:00).
- 4 tratamientos: Corte pelo corto (30m), Corte pelo largo (45m), Barba (20m), Corte + Barba (60m), con precios.
- 1 cliente demo con email/telefono/dni.

Contraseñas siempre BCrypt (nunca texto plano).

---

## 12. Estrategia de Testing (mantener 95% LINE/BRANCH)

- **Services (unit, Mockito):** `TurnoServiceImplementationTest` — reserva OK, solapamiento (409), lunes (409), fuera de jornada, fecha pasada, transiciones válidas/ inválidas, ownership. `DisponibilidadServiceTest` — slots generados, exclusión de ocupados, lunes/fecha pasada → vacío. `TratamientoServiceImplementationTest`, `PeluqueroServiceImplementationTest`.
- **Notificación:** `CompositeNotificationServiceTest` verifica que invoca cada canal; tests de cada `NotificationChannel`.
- **State/Strategy:** tests directos de cada estado y de la estrategia de slots (cubre ramas).
- **Entities:** `@PrePersist/@PreUpdate` de `TurnoEntity`.
- **Controllers/Integración:** extender `SecurityIntegrationTest` con flujo reserva→confirmar→cancelar y checks de rol/ownership (401/403).
- **Branch coverage:** cada `if` de validación necesita caso verdadero y falso (día, jornada, solape, futuro, ownership, transición).

---

## 13. Restricciones Técnicas (heredadas, no negociables)

- Inyección por constructor (`@RequiredArgsConstructor`), nunca `@Autowired` en campos.
- Todas las relaciones JPA `LAZY`.
- DTOs de respuesta sin `password` ni entidades completas.
- Toda respuesta de error usa `ErrorResponse`.
- Lógica de negocio solo en `services/implementations/`, nunca en controllers.
- Mapping Entity↔Model↔DTO vía ModelMapper (STRICT).

---

## 14. Orden de Implementación Sugerido

1. Ampliar `Role` + `UserEntity` (email/telefono/dni) + `RegisterRequestDTO` + `CustomUserDetailsService` + ajustar tests existentes.
2. `TratamientoEntity` + repo + service + controller + DTOs + tests.
3. `PeluqueroEntity` + repo + service + controller + DTOs + tests.
4. `EstadoTurno` + State + `TurnoEntity` + repo.
5. `DisponibilidadService` (Strategy) + tests.
6. `NotificationService` (Composite + canales) + tests.
7. `TurnoService` (reservar/confirmar/cancelar/completar) + controller + tests.
8. `data.sql` semillas + `SecurityIntegrationTest` extendido + verificar `mvn clean verify` ≥95%.
