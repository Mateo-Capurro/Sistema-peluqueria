# Analysis.md

> **Análisis Funcional del Proyecto — Sistema de Gestión de Turnos para Peluquería**
>
> Documento de análisis funcional. No contiene código ni decisiones de arquitectura, Angular o Spring Boot.
> Las inferencias sobre lo no especificado en la consigna se marcan como **Suposición**.

---

# 1. Resumen de la Consigna

El sistema permite a los clientes de una peluquería reservar uno o varios turnos de manera online. El cliente elige el peluquero, el tipo de tratamiento (por ejemplo corte de pelo largo, corte corto, barba), y el día y horario mediante un calendario. Al reservar, el sistema notifica al cliente por email y por WhatsApp para que confirme el turno. Para operar, el cliente debe tener una cuenta con sus datos personales y credenciales.

---

# 2. Objetivo del Sistema

**Objetivo principal:** gestionar la reserva y confirmación de turnos de una peluquería, coordinando clientes, peluqueros y tratamientos, evitando solapamientos de horario.

**El usuario debe poder:**

- Registrarse y autenticarse.
- Ver los peluqueros y tratamientos disponibles.
- Consultar un calendario con la disponibilidad de horarios.
- Reservar uno o varios turnos indicando peluquero, tratamiento y fecha/hora.
- Recibir una notificación (email + WhatsApp) para confirmar el turno.
- Confirmar o cancelar sus turnos.
- Consultar el historial y estado de sus turnos.

---

# 3. Dominio del Problema

**Dominio: Gestión / Agenda de servicios.**

Una peluquería ofrece distintos tratamientos, prestados por uno o varios peluqueros. Cada tratamiento tiene una duración determinada. Los clientes solicitan turnos para un tratamiento con un peluquero concreto en una fecha y hora. Un peluquero no puede atender dos turnos que se solapen en el tiempo. La peluquería atiende de martes a domingo (cierra los lunes). Un turno reservado queda pendiente hasta que el cliente lo confirma tras recibir la notificación.

---

# 4. Actores

| Actor | Descripción | Acciones |
|--------|-------------|----------|
| Cliente | Persona con cuenta que reserva turnos. | Registrarse, iniciar sesión, ver peluqueros/tratamientos, consultar disponibilidad, reservar turnos, confirmar, cancelar, ver sus turnos. |
| Peluquero | Profesional que atiende. Tiene cuenta y agenda propia. | Iniciar sesión, ver su agenda, confirmar/cancelar turnos asignados, marcar como completado. |
| Administrador | Gestiona el catálogo del sistema. | Iniciar sesión, gestionar peluqueros, gestionar tratamientos. |
| Sistema de Notificaciones | Actor no humano que envía avisos. | Enviar email y WhatsApp al reservar un turno. |

> **Suposición:** la consigna solo menciona explícitamente al cliente. Peluquero y Administrador se infieren como actores necesarios: el peluquero porque el cliente "selecciona con qué peluquero cortarse" (debe existir y tener agenda), y el administrador porque alguien debe cargar peluqueros y tratamientos.

---

# 5. Entidades del Dominio

| Entidad | Descripción |
|----------|-------------|
| Usuario | Cuenta con credenciales y datos personales. Base de Cliente y Peluquero. |
| Cliente | Usuario que reserva turnos. |
| Peluquero | Usuario que presta servicios y tiene agenda. |
| Tratamiento | Tipo de servicio ofrecido, con duración. |
| Turno | Reserva de un tratamiento con un peluquero en fecha/hora. |
| Notificación | Aviso enviado al cliente tras reservar. |

## Usuario
- **Descripción:** persona registrada en el sistema.
- **Responsabilidad:** guardar identidad y credenciales; permitir autenticación.
- **Relaciones:** un Cliente reserva muchos Turnos; un Peluquero atiende muchos Turnos.
- **Observaciones:** el rol distingue Cliente / Peluquero / Administrador.

## Tratamiento
- **Descripción:** servicio ofrecido (corte largo, corte corto, barba, etc.).
- **Responsabilidad:** definir el servicio y su duración (para calcular ocupación del turno).
- **Relaciones:** un Turno corresponde a un Tratamiento.
- **Observaciones:** la duración es clave para evitar solapamientos.

## Turno
- **Descripción:** cita entre un cliente y un peluquero para un tratamiento.
- **Responsabilidad:** representar la reserva, su horario y su estado.
- **Relaciones:** pertenece a un Cliente, a un Peluquero y a un Tratamiento.
- **Observaciones:** tiene estado (pendiente, confirmado, cancelado, completado).

## Peluquero
- **Descripción:** profesional con jornada laboral (horario de atención).
- **Responsabilidad:** exponer disponibilidad y atender turnos.
- **Relaciones:** tiene muchos Turnos.
- **Observaciones:** su jornada define los horarios reservables.

---

# 6. Reglas de Negocio

## Reglas explícitas

- El cliente puede reservar uno o varios turnos.
- El cliente elige el peluquero.
- El cliente elige el tipo de tratamiento.
- Los turnos se pueden sacar cualquier día de martes a domingo.
- La reserva se hace eligiendo día y horario en un calendario.
- Al reservar, se envía email y WhatsApp al cliente para confirmar.
- Para reservar, el cliente debe tener cuenta con nombre completo, email, teléfono, contraseña y DNI.

## Reglas implícitas

- Los lunes no se atiende (martes a domingo excluye lunes).
- Un peluquero no puede tener dos turnos solapados en el tiempo.
- Un turno ocupa el intervalo `[hora inicio, hora inicio + duración del tratamiento)`.
- El turno solo puede reservarse dentro de la jornada del peluquero.
- No se pueden reservar turnos en fechas/horas pasadas.
- El email y el DNI deben ser únicos por usuario.
- Un turno nace en estado pendiente y pasa a confirmado al confirmarlo el cliente.
- Solo el cliente dueño del turno (o el peluquero asignado) puede modificarlo (ownership).

---

# 7. Casos de Uso

| Caso de Uso | Actor |
|-------------|-------|
| Registrarse | Cliente |
| Iniciar sesión | Cliente, Peluquero, Administrador |
| Ver tratamientos | Cliente |
| Ver peluqueros | Cliente |
| Consultar disponibilidad | Cliente |
| Reservar turno | Cliente |
| Confirmar turno | Cliente |
| Cancelar turno | Cliente, Peluquero |
| Ver mis turnos | Cliente |
| Ver agenda | Peluquero |
| Gestionar tratamientos y peluqueros | Administrador |

## Reservar turno
- **Objetivo:** que el cliente obtenga un turno con un peluquero para un tratamiento.
- **Precondiciones:** cliente autenticado; peluquero y tratamiento existentes; horario libre y dentro de jornada; día entre martes y domingo.
- **Flujo principal:** el cliente elige peluquero → elige tratamiento → el sistema muestra el calendario con horarios libres → el cliente elige día y hora → confirma la reserva → el sistema crea el turno en estado pendiente → dispara la notificación por email y WhatsApp.
- **Flujos alternativos:** horario ya ocupado (rechaza y pide otro); día lunes o fecha pasada (rechaza); fuera de jornada (rechaza).
- **Resultado esperado:** turno creado en estado pendiente y notificación enviada.

## Confirmar turno
- **Objetivo:** que el cliente confirme un turno pendiente.
- **Precondiciones:** el turno existe, está pendiente y pertenece al cliente.
- **Flujo principal:** el cliente abre la notificación / sus turnos → confirma → el turno pasa a confirmado.
- **Flujos alternativos:** turno ya cancelado o ya confirmado (rechaza la transición).
- **Resultado esperado:** turno en estado confirmado.

---

# 8. Flujo General del Usuario

```
Inicio
   ↓
Registro / Inicio de sesión
   ↓
Selecciona peluquero y tratamiento
   ↓
Consulta calendario (horarios libres)
   ↓
Reserva día y hora  →  Turno PENDIENTE + notificación (email + WhatsApp)
   ↓
Confirma turno  →  Turno CONFIRMADO
   ↓
(Asiste)  →  Turno COMPLETADO   /   Cancela  →  Turno CANCELADO
```

---

# 9. Objetos que Manipula el Usuario

- Cuenta / Perfil
- Peluquero
- Tratamiento
- Turno
- Calendario / Disponibilidad

---

# 10. Acciones Disponibles

- Registrar cuenta
- Iniciar sesión
- Listar peluqueros
- Listar tratamientos
- Consultar disponibilidad
- Reservar turno
- Confirmar turno
- Cancelar turno
- Marcar turno como completado (peluquero)
- Listar mis turnos / agenda

---

# 11. Estados del Sistema

Estados de un **Turno**:

- Pendiente (reservado, sin confirmar)
- Confirmado
- Cancelado
- Completado

---

# 12. Eventos Importantes

- Registro de cliente
- Reserva de turno
- Envío de notificación
- Confirmación de turno
- Cancelación de turno
- Cierre / completado de turno

---

# 13. Reglas Temporales

- Solo se atiende de martes a domingo.
- Un turno ocupa desde su hora de inicio durante la duración del tratamiento.
- No se reservan turnos en fechas u horas pasadas.
- El turno debe caer dentro de la jornada del peluquero.
- Dos turnos del mismo peluquero no pueden solaparse.

---

# 14. Restricciones

- Un cliente debe estar autenticado para reservar.
- Email y DNI únicos por usuario.
- Un peluquero atiende un turno a la vez.
- No se reserva fuera de la jornada ni los lunes.
- Cada turno referencia exactamente un peluquero y un tratamiento.

---

# 15. Validaciones

- Campos obligatorios en registro: nombre completo, email, teléfono, contraseña, DNI.
- Formato de email válido y teléfono válido.
- DNI y email no duplicados.
- Fecha/hora de turno futura, día martes a domingo, dentro de jornada.
- Horario libre (sin solapamiento) para el peluquero.
- Estados de turno válidos para cada transición (no confirmar un turno cancelado, etc.).

---

# 16. Datos que Maneja el Sistema

**Usuario / Cliente**
- nombre completo
- email
- teléfono
- DNI
- contraseña (almacenada cifrada)
- rol

**Peluquero**
- datos de usuario
- jornada (hora inicio / hora fin de atención)
- estado (activo / inactivo)

**Tratamiento**
- nombre
- duración
- precio

**Turno**
- cliente
- peluquero
- tratamiento
- fecha y hora de inicio
- fecha y hora de fin
- estado

---

# 17. Relaciones entre Entidades

```
Cliente  ──(reserva muchos)──►  Turno  ◄──(atiende muchos)──  Peluquero
                                  │
                                  └──(es de un)──►  Tratamiento
```

- Un Cliente tiene muchos Turnos.
- Un Peluquero tiene muchos Turnos.
- Un Turno tiene un Tratamiento.

---

# 18. Acciones Automáticas

- Calcular la hora de fin del turno a partir de la duración del tratamiento.
- Calcular los horarios libres de un peluquero para una fecha.
- Rechazar automáticamente reservas solapadas o fuera de jornada.
- Disparar la notificación al crear el turno.

---

# 19. Casos Especiales

- Dos clientes intentan el mismo horario con el mismo peluquero (solo uno gana).
- Peluquero sin horarios libres en la fecha pedida.
- Reserva de varios turnos en una misma sesión.
- Cliente cancela un turno ya confirmado.
- Día lunes o feriado (fuera de atención).

---

# 20. Posibles Errores

- Intento de reserva fuera de turno (solapado).
- Reserva en fecha pasada o día no laborable.
- Datos de registro incompletos o duplicados.
- Confirmar/cancelar un turno que no pertenece al usuario.
- Transición de estado inválida.

---

# 21. Información que Debe Mostrar la Interfaz

- Lista de peluqueros.
- Lista de tratamientos con duración y precio.
- Calendario con días y horarios libres.
- Detalle y estado de cada turno.
- Historial de turnos del cliente / agenda del peluquero.
- Mensajes de confirmación y error.

---

# 22. Información que Debe Solicitar la Interfaz

- Datos de registro: nombre completo, email, teléfono, contraseña, DNI.
- Credenciales de inicio de sesión.
- Selección de peluquero.
- Selección de tratamiento.
- Selección de día y hora.
- Confirmaciones (reservar, confirmar, cancelar).

---

# 23. Funcionalidades Principales

| Prioridad | Funcionalidad |
|-----------|---------------|
| 1 | Registro e inicio de sesión de clientes |
| 2 | Listado de peluqueros y tratamientos |
| 3 | Consulta de disponibilidad (calendario) |
| 4 | Reserva de turno con validación de solapamiento |
| 5 | Notificación de reserva (email + WhatsApp) |
| 6 | Confirmación y cancelación de turnos |
| 7 | Agenda del peluquero |

---

# 24. Funcionalidades Secundarias

- Marcar turno como completado.
- Gestión de peluqueros y tratamientos por el administrador.
- Historial de turnos.

---

# 25. Funcionalidades Opcionales

- Reserva de varios turnos en una sola operación.
- Reprogramación de un turno.
- Recordatorio previo al turno.

---

# 26. Ambigüedades Detectadas

- **Ambigüedad:** la confirmación llega por email y WhatsApp, pero la consigna no dice si el envío es real o simulado.
  - **Suposición:** se modela el envío detrás de una interfaz de notificación con implementaciones simuladas (registran el aviso), enchufables a un proveedor real más adelante.
  - **Justificación:** permite cumplir el requisito funcional y ser verificable sin depender de servicios externos.

- **Ambigüedad:** no se especifica cómo se definen los horarios reservables.
  - **Suposición:** cada turno ocupa el tiempo que dura su tratamiento, dentro de la jornada del peluquero.
  - **Justificación:** la consigna distingue tratamientos "corte largo / corto / barba", que naturalmente duran distinto.

- **Ambigüedad:** "el cliente selecciona con qué peluquero" implica varios peluqueros, pero no se detalla su gestión.
  - **Suposición:** el peluquero es un actor con cuenta y agenda propia.
  - **Justificación:** necesario para tener disponibilidad por peluquero y ownership de la agenda.

---

# 27. Suposiciones

- Los lunes la peluquería no atiende.
- El envío de email y WhatsApp se simula tras una interfaz.
- Cada peluquero tiene una jornada configurable.
- El turno se confirma desde la propia aplicación tras recibir el aviso.
- El "id" mencionado por el cliente corresponde al identificador interno del usuario/turno, generado por el sistema.

---

# 28. Glosario

- **Turno:** reserva de un tratamiento con un peluquero en una fecha y hora.
- **Tratamiento:** servicio ofrecido (corte largo, corte corto, barba, etc.), con una duración.
- **Peluquero:** profesional que presta los tratamientos y tiene agenda.
- **Jornada:** franja horaria en la que un peluquero atiende.
- **Disponibilidad:** conjunto de horarios libres de un peluquero en una fecha.
- **Solapamiento:** superposición temporal de dos turnos del mismo peluquero.
- **Notificación:** aviso (email + WhatsApp) enviado al cliente al reservar.

---

# 29. Resumen Ejecutivo

- **¿Qué hace el sistema?** Gestiona la reserva y confirmación de turnos de una peluquería, coordinando clientes, peluqueros y tratamientos, y evitando solapamientos de horario.
- **¿Quién lo usa?** Clientes (reservan), peluqueros (atienden su agenda) y un administrador (gestiona el catálogo).
- **¿Qué objetos existen?** Usuario/Cliente, Peluquero, Tratamiento, Turno y Notificación.
- **¿Qué reglas importantes hay?** Atención de martes a domingo, sin solapamientos por peluquero, turno ocupa la duración del tratamiento, confirmación posterior a la reserva, ownership de los turnos.
- **¿Qué funcionalidades tendrá?** Registro/login, catálogo de peluqueros y tratamientos, calendario de disponibilidad, reserva con validación, notificación y confirmación/cancelación.

---

# 30. Preparación para la Arquitectura

**Probables entidades:** Usuario, Peluquero, Tratamiento, Turno.

**Probables modelos:** Usuario/Cliente, Peluquero, Tratamiento, Turno, Estado de turno, Disponibilidad, Notificación.

**Probables servicios:** autenticación, gestión de usuarios, gestión de peluqueros, gestión de tratamientos, reserva/gestión de turnos, cálculo de disponibilidad, notificación.

**Probables pantallas:** registro, inicio de sesión, catálogo de peluqueros/tratamientos, calendario de reserva, mis turnos, agenda del peluquero, gestión (admin).

**Probables componentes:** formularios de registro/login, lista de peluqueros, lista de tratamientos, calendario/selección de horario, tarjeta de turno, panel de agenda.

*No se diseñan todavía; solo se listan.*

---

# Checklist Final

- [x] Toda la consigna fue analizada.
- [x] Todos los actores fueron identificados.
- [x] Todas las entidades fueron identificadas.
- [x] Todas las reglas fueron documentadas.
- [x] Todos los casos de uso fueron identificados.
- [x] Todas las restricciones fueron documentadas.
- [x] Todas las validaciones fueron documentadas.
- [x] Todas las ambigüedades fueron registradas.
- [x] Todas las suposiciones fueron justificadas.
- [x] El dominio del problema quedó completamente comprendido.

---

# Criterios de Calidad

Documento sin código, sin decisiones de Angular ni Spring Boot, sin componentes/servicios/arquitectura ni diseño visual. Centrado en el problema de negocio.
