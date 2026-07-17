# Sistema de Peluquería — Salón Alma

Aplicación web full-stack para la gestión de turnos de una peluquería. Los clientes
reservan citas online, los peluqueros gestionan su agenda y los administradores
mantienen el catálogo de servicios y el equipo de estilistas.

El proyecto está construido sobre un scaffold de autenticación (Spring Boot + Angular)
con JWT, roles y refresh tokens, extendido con el dominio de turnos.

---

## Tabla de contenidos

- [Funcionalidad](#funcionalidad)
- [Stack tecnológico](#stack-tecnológico)
- [Arquitectura y patrones](#arquitectura-y-patrones)
- [Estructura del proyecto](#estructura-del-proyecto)
- [Puesta en marcha](#puesta-en-marcha)
- [Usuarios de prueba](#usuarios-de-prueba)
- [API](#api)
- [Testing y cobertura](#testing-y-cobertura)

---

## Funcionalidad

El sistema maneja tres roles, cada uno con su vista:

| Rol | Puede hacer |
|-----|-------------|
| **CLIENTE** | Ver servicios y estilistas, reservar cita (elige estilista, servicio, fecha y horario disponible), ver/confirmar/cancelar sus citas. |
| **PELUQUERO** | Ver su agenda de citas asignadas, completar o cancelar turnos. |
| **ADMIN** | Administrar servicios (CRUD) y estilistas (alta de peluquero, jornada laboral, activar/desactivar). |

Otras características:

- **Autenticación JWT** con access token de corta duración + refresh token persistido.
- **Cálculo de disponibilidad**: dado un estilista, un servicio y una fecha, el backend
  calcula los horarios libres respetando la jornada del peluquero, la duración del
  servicio y los turnos ya ocupados. Los lunes el salón está cerrado.
- **Notificaciones por email** al reservar y confirmar un turno (SMTP configurable;
  desactivado por defecto en desarrollo).
- **Confirmación por link**: el email de reserva incluye un enlace que confirma la cita.
- **Persistencia real** en H2 en modo archivo — los turnos y tokens sobreviven a reinicios.

---

## Stack tecnológico

### Backend

| Tecnología | Uso |
|------------|-----|
| **Java 21** | Lenguaje |
| **Spring Boot 4.1** | Framework (Web MVC, Data JPA, Security, Validation, Mail) |
| **Spring Security + JWT (jjwt 0.12)** | Autenticación y autorización por roles |
| **H2 Database** (modo archivo) | Persistencia |
| **Hibernate / JPA** | ORM |
| **ModelMapper** | Mapeo entidad ↔ DTO |
| **Lombok** | Reducción de boilerplate |
| **springdoc-openapi (Swagger UI)** | Documentación de la API |
| **Spring Boot Starter Mail** | Envío de emails (SMTP) |
| **Maven** | Build |
| **JUnit + Spring Security Test + JaCoCo** | Testing y cobertura (mínimo 95%) |

### Frontend

| Tecnología | Uso |
|------------|-----|
| **Angular 22** (standalone components) | Framework SPA |
| **TypeScript 6** | Lenguaje |
| **Angular Signals** | Estado reactivo en componentes |
| **RxJS** | HTTP y flujos asíncronos |
| **Tailwind CSS 3.4** | Estilos (tema editorial "Salón Alma": tipografía Playfair Display + DM Sans, paleta oro/crema) |
| **Angular Router + Guards** | Navegación y protección de rutas |
| **Vitest** | Testing |

---

## Arquitectura y patrones

### Backend — arquitectura en capas

```
controllers  →  services (interfaces + implementations)  →  repositories  →  H2
                     ↑
                   dtos  (entrada/salida; nunca se exponen entidades crudas)
```

Paquetes: `config`, `controllers`, `domain/entities`, `domain/models`, `dtos`,
`exceptions`, `repositories`, `security`, `services`.

**Patrones de diseño aplicados:**

- **Strategy** — cálculo de slots de disponibilidad: `SlotCalculationStrategy`
  (interfaz) con la implementación `FixedStepSlotStrategy`. Permite cambiar el
  algoritmo de generación de horarios sin tocar el servicio.
- **Composite + Channel** — notificaciones: `NotificationService` con
  `CompositeNotificationService` que despacha a varios `NotificationChannel`
  (`EmailNotificationChannel`, `WhatsappNotificationChannel`). Agregar un canal
  no requiere modificar la lógica existente.
- **DTO** — toda la comunicación REST usa DTOs; las entidades JPA no se exponen.
- **Repository** — acceso a datos vía Spring Data JPA.
- **Interface + Implementation** — cada servicio separa contrato de implementación
  (`TurnoService` / `TurnoServiceImplementation`, etc.).

### Frontend — organización Angular

- **components/** — un componente por pantalla (login, register, home, reserva,
  agenda, mis-turnos, listados y paneles de admin) más dos layouts (`auth-layout`,
  `main-layout`).
- **shared/services/** — servicios HTTP inyectables (`auth`, `turno`, `tratamiento`,
  `peluquero`).
- **shared/guards/** — `auth.guard` (requiere sesión) y `role.guard` (requiere rol).
- **shared/interceptors/** — `auth.interceptor` (adjunta el JWT) y `error.interceptor`
  (manejo centralizado de errores).
- **shared/models/** — interfaces de tipado (auth, user, turno, tratamiento, peluquero).

La sesión se persiste en el navegador, así que el usuario no vuelve a loguearse en cada acción.

---

## Estructura del proyecto

```
Sistema-peluqueria/
└── ScaffoldingBE-FE/
    ├── BE/parcial/              # Backend Spring Boot
    │   ├── src/main/java/be/parcial/
    │   │   ├── config/          # CORS, seguridad, beans
    │   │   ├── controllers/     # AuthController, TurnoController, TratamientoController, PeluqueroController
    │   │   ├── domain/          # entities/ (JPA) y models/
    │   │   ├── dtos/
    │   │   ├── exceptions/
    │   │   ├── repositories/
    │   │   ├── security/        # JWT, filtros
    │   │   └── services/        # lógica de negocio (Strategy de disponibilidad, notificaciones)
    │   ├── src/main/resources/
    │   │   ├── application.properties
    │   │   └── data.sql         # datos de ejemplo (usuarios, servicios, turno demo)
    │   └── src/test/            # tests backend (JaCoCo ≥ 95%)
    ├── FE/                      # Frontend Angular
    │   ├── src/app/components/
    │   ├── src/app/shared/
    │   ├── public/salon-bg.jpg  # imagen de fondo del tema
    │   ├── proxy.conf.json      # proxy /api → http://localhost:8080
    │   └── tailwind.config.js   # paleta y tipografía "Salón Alma"
    └── start-dev.ps1            # arranca backend + frontend con puertos limpios
```

---

## Puesta en marcha

Requisitos: **JDK 21** y **Node.js** (con npm).

### Opción rápida (recomendada, Windows)

Desde `ScaffoldingBE-FE/`:

```powershell
./start-dev.ps1
```

Libera los puertos 8080 y 4200 (mata procesos huérfanos), arranca el backend,
espera a que esté listo y luego levanta el frontend — cada uno en su ventana.

### Manual

**Backend** (puerto 8080):

```bash
cd ScaffoldingBE-FE/BE/parcial
./mvnw spring-boot:run        # Windows: ./mvnw.cmd spring-boot:run
```

**Frontend** (puerto 4200):

```bash
cd ScaffoldingBE-FE/FE
npm install
npm start
```

Abrir **http://localhost:4200**. El frontend proxya `/api` al backend en el 8080
(ver `proxy.conf.json`), por eso el backend debe estar corriendo primero.

### Recursos útiles

- Swagger UI: http://localhost:8080/swagger-ui.html
- Consola H2: http://localhost:8080/h2-console (JDBC: `jdbc:h2:file:~/peluqueria-db/parcialdb`, user `sa`, sin password)

### Email (opcional)

El envío real de emails está apagado por defecto. Para activarlo, definir variables
de entorno (`MAIL_ENABLED=true`, `MAIL_USERNAME`, `MAIL_PASSWORD`, …) o un
`secrets.properties` en el classpath (gitignored).

---

## Usuarios de prueba

Sembrados en `data.sql`:

| Usuario | Contraseña | Rol |
|---------|-----------|-----|
| `admin` | `admin123` | ADMIN |
| `user` | `user123` | CLIENTE |
| `jdoe` | `admin123` | CLIENTE |
| `mrodriguez` | `admin123` | PELUQUERO |
| `lgomez` | `admin123` | PELUQUERO |

`mrodriguez` tiene un turno demo asignado.

---

## API

Endpoints principales (base `/api`):

| Método | Ruta | Descripción |
|--------|------|-------------|
| `POST` | `/auth/register` | Registro de cliente |
| `POST` | `/auth/authenticate` | Login (devuelve access + refresh token) |
| `POST` | `/auth/refresh` | Renovar access token |
| `POST` | `/auth/logout` | Cerrar sesión |
| `GET` | `/tratamientos` · `/tratamientos/{id}` | Listar / ver servicio |
| `POST` · `PUT` · `DELETE` | `/tratamientos` · `/tratamientos/{id}` | CRUD de servicios (ADMIN) |
| `GET` | `/peluqueros` · `/peluqueros/{id}` | Listar / ver estilista |
| `PUT` | `/peluqueros/{id}` | Editar jornada/estado (ADMIN) |
| `GET` | `/turnos/disponibilidad` | Horarios libres para estilista/servicio/fecha |
| `POST` | `/turnos` | Reservar turno |
| `GET` | `/turnos/mios` | Citas del cliente autenticado |
| `GET` | `/turnos/agenda` | Agenda del peluquero autenticado |
| `PATCH` | `/turnos/{id}/confirmar` · `/cancelar` · `/completar` | Cambiar estado del turno |
| `POST` | `/turnos/confirmar/token/{token}` | Confirmar cita vía link del email |

La documentación completa e interactiva está en Swagger UI.

---

## Testing y cobertura

Backend:

```bash
cd ScaffoldingBE-FE/BE/parcial
./mvnw clean verify
```

El build de JaCoCo exige **95% de cobertura** de líneas y ramas; falla por debajo
de ese umbral. Hay ~23 clases de test que cubren servicios, controllers y seguridad.
