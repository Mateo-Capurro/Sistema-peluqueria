# Architecture.md

> **Arquitectura FrontEnd**
>
> Este documento define la arquitectura técnica del FrontEnd.
> Debe completarse **después de finalizar `analysis.md`** y **antes de comenzar cualquier implementación**.
>
> **Este proyecto únicamente contempla el FrontEnd.**
> El Backend ya existe o será desarrollado por separado y **no debe modificarse**.
>
> El objetivo de este documento es definir una arquitectura consistente para que toda la implementación Angular siga las mismas reglas.

---

# Instrucciones para la IA

Antes de completar este documento debes:

1. Leer completamente:
   - analysis.md
   - sdd.md
   - design.md
2. No escribir código.
3. No crear archivos.
4. No modificar el Backend.
5. Basar todas las decisiones en la consigna.
6. Si una decisión no está especificada, justificarla.

---

# 1. Resumen Arquitectónico

Explicar brevemente cómo estará organizado el FrontEnd.

Debe responder:

- ¿Qué tipo de aplicación es?
- ¿Cómo estará organizada?
- ¿Qué patrón arquitectónico utilizará?
- ¿Cómo se comunicará con el Backend?

---

# 2. Tecnologías

La IA debe confirmar las tecnologías definidas en el SDD.

## Framework

Angular 21

## Arquitectura

Standalone Components

## Estado

Signals

## Routing

Angular Router

## HTTP

HttpClient

## Estilos

SCSS

## Build

Angular CLI

No proponer tecnologías distintas.

---

# 3. Organización del Proyecto

La IA debe completar la estructura del proyecto.

Ejemplo.

```text
src/

app/

core/

shared/

features/

layout/

models/

services/

guards/

interceptors/

utils/

assets/

```

Para cada carpeta explicar:

- Responsabilidad
- Qué puede contener
- Qué NO debe contener

---

# 4. Arquitectura por Capas

Explicar las capas.

Ejemplo.

```
Views

↓

Components

↓

Services

↓

API

↓

Backend
```

Describir la responsabilidad de cada capa.

---

# 5. Pantallas

La IA debe identificar todas las pantallas necesarias.

Completar.

| Pantalla | Objetivo |
|----------|----------|

Para cada una indicar:

- Responsabilidad
- Ruta
- Componentes principales
- Servicios utilizados

---

# 6. Layouts

Listar los layouts necesarios.

Ejemplo.

GameLayout

MenuLayout

SettingsLayout

MainLayout

Para cada uno indicar.

- Objetivo
- Componentes
- Responsabilidad

---

# 7. Componentes

Identificar todos los componentes.

Completar.

| Componente | Tipo | Reutilizable |
|------------|------|--------------|

Tipos.

- Página
- Layout
- Compartido
- Feature
- UI
- Modal

---

## Para cada componente

Nombre

Responsabilidad

Inputs

Outputs

Signals utilizadas

Servicios utilizados

Estados visuales

Eventos

Dependencias

---

# 8. Componentes Compartidos

Identificar los componentes reutilizables.

Ejemplo.

Button

Card

Modal

Dialog

Badge

Spinner

Toast

Tooltip

Loader

EmptyState

Skeleton

Avatar

Chip

---

# 9. Componentes de Dominio

Listar únicamente los componentes propios del negocio.

Ejemplo.

Board

Dice

PlayerCard

Inventory

GameLog

---

# 10. Modelos

Listar todos los modelos utilizados en el FrontEnd.

Para cada uno indicar.

Nombre

Descripción

Propiedades

Relaciones

---

# 11. Interfaces

Listar las interfaces.

Responsabilidad.

Dónde se utilizan.

---

# 12. Estado de la Aplicación

La IA debe definir.

Estado Global

Estado Local

Estado Temporal

Estado Derivado

---

## Signals

Listar todas las Signals necesarias.

Ejemplo.

```
playersSignal

boardSignal

selectedPlayerSignal

loadingSignal

```

Explicar qué almacena cada una.

---

# 13. Servicios

Listar todos los servicios del FrontEnd.

Para cada uno indicar.

Responsabilidad

Métodos principales

Dependencias

Estado que administra

Comunicación HTTP

---

# 14. Consumo del Backend

El Backend NO debe modificarse.

La IA debe listar.

Servicios HTTP

Endpoints utilizados

Modelos utilizados

Errores posibles

Sin implementar lógica del Backend.

---

# 15. Navegación

Definir el flujo de navegación.

Ejemplo.

```
Home

↓

Game

↓

Summary

↓

Settings
```

---

# 16. Comunicación entre Componentes

Definir cuándo utilizar.

- Input
- Output
- Signals
- Servicios
- ViewChild (solo si es necesario)

Evitar acoplamiento innecesario.

---

# 17. Flujo de Datos

Representar.

Ejemplo.

```
Usuario

↓

Componente

↓

Servicio

↓

API

↓

Respuesta

↓

Signal

↓

Vista
```

---

# 18. Responsabilidades

Cada componente debe cumplir.

- Una única responsabilidad.
- No acceder directamente al Backend.
- No contener lógica de negocio compleja.
- Delegar acceso a datos a Services.
- Ser reutilizable cuando sea posible.

---

# 19. Manejo de Estados

Definir.

Loading

Error

Vacío

Éxito

Actualización

---

# 20. Manejo de Errores

Describir.

Errores HTTP.

Errores de validación.

Errores inesperados.

Mensajes al usuario.

---

# 21. Performance

La IA debe considerar.

- Signals
- Control Flow moderno
- track en @for
- Lazy Loading cuando corresponda
- Evitar renders innecesarios
- Componentes pequeños

---

# 22. Accesibilidad

Debe cumplir.

- Navegación por teclado
- Contraste
- Focus visible
- Etiquetas accesibles
- Responsive

---

# 23. Convenciones

## Componentes

```
board.component.ts
```

## Servicios

```
board.service.ts
```

## Interfaces

```
board.interface.ts
```

## Models

```
board.model.ts
```

## Signals

Usar nombres descriptivos.

Ejemplo.

```
gameStateSignal

currentPlayerSignal

```

---

# 24. Dependencias

La IA debe indicar.

Qué componentes dependen de otros.

Qué servicios utilizan.

Evitar dependencias circulares.

---

# 25. Riesgos

Identificar.

Componentes demasiado grandes.

Duplicación.

Estado duplicado.

Acoplamiento.

Renderizados innecesarios.

Proponer solución.

---

# 26. Decisiones Arquitectónicas

La IA debe justificar.

¿Por qué esa estructura?

¿Por qué esos componentes?

¿Por qué esos servicios?

¿Por qué esa organización?

---

# 27. Checklist Final

Antes de comenzar la implementación verificar.

- [ ] Todas las pantallas identificadas.
- [ ] Todos los layouts definidos.
- [ ] Todos los componentes definidos.
- [ ] Todos los modelos definidos.
- [ ] Todas las interfaces definidas.
- [ ] Todos los servicios definidos.
- [ ] Navegación definida.
- [ ] Estado definido.
- [ ] Comunicación entre componentes definida.
- [ ] Arquitectura consistente con analysis.md.
- [ ] Arquitectura consistente con sdd.md.
- [ ] Arquitectura consistente con design.md.

---

# Restricciones para la IA

Durante este documento:

- No escribir código.
- No crear componentes.
- No crear servicios.
- No implementar pantallas.
- No modificar el Backend.
- No cambiar las tecnologías definidas en el SDD.
- No inventar funcionalidades que no existan en la consigna.
- Documentar cualquier decisión inferida.

El objetivo de este documento es que, una vez aprobado, la implementación del FrontEnd pueda realizarse sin tomar nuevas decisiones arquitectónicas.
