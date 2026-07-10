# SDD - Backend (Scaffolding de Autenticación)

## 1. Objetivo del Backend
API REST segura con autenticación JWT (Spring Security + filtro personalizado). Sirve como scaffolding para futuros proyectos. Exclusivamente endpoints de autenticación (`/api/auth/...`).

---

## 2. Stack Tecnológico (Inamovible)
- **Lenguaje:** Java 21 (LTS).
- **Framework:** Spring Boot 4.1.0.
- **Gestor de dependencias:** Maven.
- **Seguridad:** Spring Security + JWT (JJWT 0.12.6) con filtro personalizado `JwtAuthenticationFilter`.
- **Persistencia:** Spring Data JPA (Hibernate).
- **Base de datos:** H2 (en memoria, consola en `/h2-console`).
- **Mapping:** ModelMapper 3.2.0 (STRICT, field matching, skip null).
- **Documentación:** SpringDoc OpenAPI 2.8.6 (Swagger UI en `/swagger-ui.html`).
- **Testing:** JUnit 5, Mockito, Spring Boot Test, AssertJ.
- **Cobertura:** JaCoCo 0.8.12 (mínimo 95% LINE, 95% BRANCH).

---

## 3. Estructura de Paquetes (Obligatoria)
El código fuente (`src/main/java/be/parcial/`) sigue esta estructura:

- `config/` → Clases de configuración de Spring (`SecurityConfig`, `CorsConfig`, `MappersConfig`, `OpenApiConfig`).
- `controllers/` → Controladores REST (solo manejan peticiones/respuestas, sin lógica de negocio).
- `domain/` → Modelo de dominio del negocio.
  - `domain/entities/` → Entidades JPA (mapeo a tablas).
  - `domain/models/` → Modelos intermedios para mapeo Entity↔DTO vía ModelMapper.
- `dtos/` → Objetos de transferencia de datos (petición y respuesta).
- `exceptions/` → Manejador global de excepciones y excepciones personalizadas.
- `repositories/` → Interfaces que extienden `JpaRepository`.
- `security/` → Filtros JWT y servicio de tokens (`JwtService`, `JwtAuthenticationFilter`, `CustomUserDetailsService`).
- `services/` → Interfaces de servicios.
- `services/implementations/` → Implementaciones concretas de los servicios (lógica de negocio).

---

## 4. Modelo de Datos (Entidades JPA)

### 4.1. Entidad `UserEntity` (Tabla: `users`)
| Campo | Tipo Java | Anotaciones JPA | Restricciones |
| :--- | :--- | :--- | :--- |
| `id` | `Long` | `@Id`, `@GeneratedValue(strategy = GenerationType.IDENTITY)` | Autoincremental. |
| `username` | `String` | `@Column(nullable = false, unique = true, length = 50)` | Identificador único de autenticación. |
| `password` | `String` | `@Column(nullable = false)` | Almacenado con `BCryptPasswordEncoder`. No devolver en respuestas. |
| `name` | `String` | `@Column(nullable = false, length = 100)` | Nombre visible del usuario. |
| `role` | `Enum (Role)` | `@Enumerated(EnumType.STRING)` | `USER` o `ADMIN`. Inner enum de `UserEntity`. |
| `createdAt` | `LocalDateTime` | `@Column(nullable = false, updatable = false)` | `@PrePersist` autogenerado. |
| `updatedAt` | `LocalDateTime` | `@Column(nullable = false)` | `@PrePersist` / `@PreUpdate` autogenerado. |

### 4.2. Entidad `RefreshTokenEntity` (Tabla: `refresh_token`)
| Campo | Tipo Java | Anotaciones JPA | Restricciones |
| :--- | :--- | :--- | :--- |
| `id` | `Long` | `@Id`, `@GeneratedValue(strategy = GenerationType.IDENTITY)` | Autoincremental. |
| `token` | `String` | `@Column(nullable = false, unique = true, length = 500)` | JWT generado por `JwtService`. |
| `user` | `UserEntity` | `@ManyToOne(fetch = FetchType.LAZY)` | Join column `user_id`. |
| `expiryDate` | `LocalDateTime` | `@Column(nullable = false)` | Duración de 7 días. |
| `revoked` | `boolean` | `@Column(nullable = false)` | `false` por defecto. Se marca `true` al hacer logout/refresh. |
| `createdAt` | `LocalDateTime` | `@Column(nullable = false, updatable = false)` | `@PrePersist` autogenerado. |

---

## 5. Contratos de API (Endpoints REST)

### 5.1. Autenticación

**Base Path:** `/api/auth`

| Método | Endpoint | Autenticación | Descripción |
| :--- | :--- | :--- | :--- |
| `POST` | `/register` | Público | Registra un nuevo usuario. Devuelve `AuthResponse` con tokens. |
| `POST` | `/authenticate` | Público | Autentica por username+password. Devuelve `AuthResponse`. |
| `POST` | `/refresh` | Público (requiere refresh token válido) | Renueva access token y refresh token. |
| `POST` | `/logout` | Público (requiere refresh token) | Invalida el refresh token en BD (revocado). |

---

## 6. DTOs (Objetos de Transferencia)

**Requests:**
- `RegisterRequestDTO`: `{ username, password, name }` — `username` 3-50 chars, `password` mínimo 6 caracteres.
- `AuthRequestDTO`: `{ username, password }` — usado en `/authenticate`.
- `RefreshTokenRequestDTO`: `{ refreshToken }`.

**Responses:**
- `AuthResponseDTO`: `{ accessToken, refreshToken, tokenType, username, role }` — **NUNCA incluye password**.
- `ErrorResponse`: `{ status (int), error (string), message (string), path (string), timestamp, details }` — formato obligatorio para todas las respuestas de error.

---

## 7. Capa de Seguridad (JWT + Spring Security)

### 7.1. Configuración (`SecurityConfig`)
- CSRF deshabilitado.
- `SessionCreationPolicy.STATELESS`.
- Endpoints públicos: `/api/auth/**`, `/h2-console/**`, `/swagger-ui/**`, `/v3/api-docs/**`.
- Resto de endpoints requieren autenticación (`authenticated()`).
- `JwtAuthenticationFilter` añadido **antes** de `UsernamePasswordAuthenticationFilter`.
- `DaoAuthenticationProvider` con `BCryptPasswordEncoder`.

### 7.2. Filtro (`JwtAuthenticationFilter`)
- Extrae el token del header `Authorization: Bearer {token}`.
- Extrae el username (subject del JWT) vía `JwtService.extractUsername()`.
- Carga `UserDetails` desde BD vía `CustomUserDetailsService.loadUserByUsername(username)`.
- Si el token es válido, establece `UsernamePasswordAuthenticationToken` en `SecurityContextHolder`.

### 7.3. Servicio de Tokens (`JwtService`)
- Algoritmo: `HS256` (HMAC-SHA).
- Clave secreta: `jwt.secret` desde `application.properties` (Base64, mínimo 32 bytes).
- Access token: `900000` ms (15 minutos).
- Refresh token: `604800000` ms (7 días).
- Issuer: `parcial-app`.
- Claims: `subject = username`, `issuedAt`, `expiration`, `jti` (solo refresh).

### 7.4. UserDetails personalizado (`CustomUserDetailsService`)
- Implementa `UserDetailsService`.
- Busca por username en `UserRepository`.
- Retorna `User` de Spring Security con `ROLE_USER` o `ROLE_ADMIN`.

### 7.5. Codificación de contraseñas
- Siempre `BCryptPasswordEncoder` (fuerza por defecto = 10).

---

## 8. Manejador Global de Excepciones (`GlobalExceptionHandler`)

Usa `@RestControllerAdvice` para capturar:

| Excepción | Status | Descripción |
| :--- | :--- | :--- |
| `ResourceNotFoundException` | `404` (Not Found) | Entidad no encontrada. |
| `MethodArgumentNotValidException` | `400` (Bad Request) | Errores de validación con detalles campo por campo. |
| `BadCredentialsException` | `401` (Unauthorized) | Credenciales inválidas. |
| `IllegalArgumentException` | `400` (Bad Request) | Errores de negocio (usuario duplicado, token expirado/revocado). |
| `Exception` (genérica) | `500` (Internal Server Error) | Error no esperado (no expone stacktrace). |

Todas devuelven el DTO `ErrorResponse` con estructura `{ status, error, message, path, timestamp, details }`.

---

## 9. Perfiles y Configuración

### `application.properties` (Desarrollo — H2)
```properties
spring.application.name=parcial
server.port=8080
spring.datasource.url=jdbc:h2:mem:parcialdb
spring.datasource.driverClassName=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=
spring.h2.console.enabled=true
spring.h2.console.path=/h2-console
spring.jpa.database-platform=org.hibernate.dialect.H2Dialect
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=false
spring.jpa.defer-datasource-initialization=true
spring.sql.init.mode=always

# JWT
jwt.secret=dGVzdC1zZWNyZXQta2V5LWZvci1kZXZlbG9wbWVudC1vbmx5LW1pbmltdW0tMzItYml0cw==
jwt.access-token-expiration=900000
jwt.refresh-token-expiration=604800000
jwt.issuer=parcial-app

# CORS
cors.allowed-origins=http://localhost:4200,http://localhost:3000

# Swagger / OpenAPI
springdoc.api-docs.path=/v3/api-docs
springdoc.swagger-ui.path=/swagger-ui.html
```

### Seed Data
`src/main/resources/data.sql` inserta datos de ejemplo al iniciar la aplicación:
- 3 usuarios (admin, user, jdoe) con contraseñas BCrypt.

**Nota:** En producción, migrar a variables de entorno y usar PostgreSQL con `spring.jpa.hibernate.ddl-auto=validate`.

---

## 10. Dependencias Maven (pom.xml) — Claves

| Dependencia | Versión | Propósito |
| :--- | :--- | :--- |
| `spring-boot-starter-webmvc` | (parent) | REST controllers |
| `spring-boot-starter-data-jpa` | (parent) | JPA + Hibernate |
| `spring-boot-starter-security` | (parent) | Spring Security |
| `spring-boot-starter-validation` | (parent) | `@Valid`, `@NotBlank`, etc. |
| `jjwt-api`, `jjwt-impl`, `jjwt-jackson` | 0.12.6 | JWT generación/validación |
| `modelmapper` | 3.2.0 | Entity↔Model↔DTO mapping |
| `h2` | (parent) | Base de datos en memoria (runtime) |
| `springdoc-openapi-starter-webmvc-ui` | 2.8.6 | Swagger UI |
| `jackson-datatype-jsr310` | (parent) | Serialización `LocalDateTime` |
| `lombok` | (parent) | `@Data`, `@RequiredArgsConstructor`, etc. |
| `spring-boot-starter-test` | (parent) | JUnit 5, Mockito, AssertJ |
| `spring-security-test` | (parent) | `@WithMockUser`, etc. |

---

## 11. Estrategia de Testing

### Unitarios (`/src/test/java/.../services/`)
- Usan `@ExtendWith(MockitoExtension.class)`.
- Mockean repositorios y prueban lógica de negocio en `ServiceImpl`.
- Verifican que las contraseñas se cifran antes de guardar.

### Unitarios de Seguridad (`/src/test/java/.../security/`)
- `JwtServiceTest`: genera tokens, extrae claims, valida expiración.
- `JwtAuthenticationFilterTest`: simula peticiones con/sin token.
- `CustomUserDetailsServiceTest`: carga usuario por username.

### Integración (`/src/test/java/.../`)
- `SecurityIntegrationTest`: flujo completo registro → login → refresh → logout.

### Otras capas
- `entities/UserEntityTest`: verifica callbacks `@PrePersist`/`@PreUpdate`.
- `exceptions/GlobalExceptionHandlerTest`: verifica respuestas de error.
- `ParcialApplicationTests`: verifica que el contexto de Spring carga.

### Cobertura
- Mínimo: **95% LINE**, **95% BRANCH** (vía JaCoCo).

---

## 12. Comandos de Ejecución

```bash
# Levantar en desarrollo (H2)
mvn spring-boot:run

# Ejecutar tests
mvn clean test

# Verificar cobertura
mvn clean verify

# Empaquetar JAR
mvn clean package

# Levantar JAR empaquetado
java -jar target/parcial-0.0.1-SNAPSHOT.jar
```

---

## 13. Restricciones Técnicas NO NEGOCIABLES

- ✅ El `JwtAuthenticationFilter` **NO** lanza excepciones directamente; continúa el chain si no hay token o es inválido.
- ✅ Todas las relaciones JPA son `LAZY`.
- ✅ No se usa `@Autowired` en campos; solo inyección por constructor (vía Lombok `@RequiredArgsConstructor`).
- ✅ Las contraseñas en texto plano están **prohibidas** en cualquier log o respuesta JSON.
- ✅ `application.properties` puede contener valores por defecto para desarrollo; en producción usar placeholders `${...}`.
- ✅ Los DTOs de respuesta **nunca** incluyen el campo `password`.
- ✅ Toda respuesta de error usa el formato `ErrorResponse` unificado.
