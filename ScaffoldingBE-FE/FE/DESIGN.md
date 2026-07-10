# Design.md

> **Design System del Proyecto**
>
> Este documento define las reglas visuales del FrontEnd.
>
> **No contiene código.**
>
> El objetivo es que toda la aplicación tenga una identidad visual consistente sin que la IA improvise estilos durante la implementación.
>
> Este documento **NO define layouts**, **NO define arquitectura** y **NO define lógica de negocio**.
>
> Solo responde una pregunta:
>
> **¿Cómo debe verse la aplicación?**

---

# Instrucciones para la IA

Antes de completar este documento debes:

1. Leer completamente:
   - analysis.md
   - architecture.md
   - sdd.md
2. Si existen mockups, wireframes o capturas de diseño, utilizarlos como referencia principal.
3. No modificar la arquitectura.
4. No escribir código.
5. Mantener consistencia visual en toda la aplicación.
6. Si un estilo no está definido, elegir una opción moderna y consistente y documentarla.

---

# 1. Identidad Visual

Completar.

## Estilo general

Seleccionar.

- [ ] Minimalista
- [ ] Material Design
- [ ] Flat Design
- [ ] Dashboard
- [ ] Videojuego
- [ ] Administrativo
- [ ] Moderno
- [ ] Otro

Explicar.

---

## Personalidad

Seleccionar.

- Profesional
- Elegante
- Divertida
- Moderna
- Tecnológica
- Casual
- Retro
- Oscura
- Colorida

Explicar.

---

# 2. Paleta de Colores

## Color Primario

Completar.

Uso.

---

## Color Secundario

Completar.

Uso.

---

## Color de Éxito

Uso.

---

## Color de Advertencia

Uso.

---

## Color de Error

Uso.

---

## Color de Información

Uso.

---

## Colores de Fondo

Fondo principal

Fondo secundario

Paneles

Tarjetas

---

## Colores de Texto

Principal

Secundario

Deshabilitado

Enlaces

---

# 3. Tipografía

Fuente principal.

Fuente secundaria.

Peso.

Jerarquía.

Ejemplo.

Título principal

Título secundario

Subtítulo

Texto

Ayuda

Botones

Etiquetas

---

# 4. Espaciado

Definir una escala.

Ejemplo.

4px

8px

12px

16px

24px

32px

48px

64px

Indicar cuándo utilizar cada uno.

---

# 5. Bordes

Border Radius.

Pequeño.

Mediano.

Grande.

Botones.

Cards.

Inputs.

Modales.

---

# 6. Sombras

Definir.

Sin sombra.

Ligera.

Media.

Alta.

Cuándo utilizar cada una.

---

# 7. Iconografía

Definir.

Librería.

Tamaño.

Uso.

Consistencia.

---

# 8. Botones

Definir estilos.

## Primario

Uso.

Estados.

Hover.

Disabled.

Loading.

---

## Secundario

Uso.

---

## Terciario

Uso.

---

## Danger

Uso.

---

## Icon Button

Uso.

---

# 9. Inputs

Definir.

Text

Password

Search

Number

Textarea

Select

Checkbox

Radio

Switch

Date

Estados.

Normal.

Focus.

Error.

Disabled.

---

# 10. Tarjetas (Cards)

Definir.

Contenido.

Espaciado.

Título.

Acciones.

Sombras.

Hover.

---

# 11. Paneles

Definir.

Fondo.

Padding.

Separación.

Bordes.

Scroll.

---

# 12. Tablas

Definir.

Cabecera.

Filas.

Hover.

Orden.

Filtros.

Estados vacíos.

---

# 13. Formularios

Definir.

Distribución.

Etiquetas.

Mensajes.

Errores.

Botones.

---

# 14. Modales

Definir.

Tamaño.

Fondo.

Animación.

Botones.

Cierre.

---

# 15. Tooltips

Definir.

Color.

Posición.

Duración.

---

# 16. Toasts

Definir.

Éxito.

Error.

Advertencia.

Información.

Duración.

Posición.

---

# 17. Loader

Definir.

Spinner.

Skeleton.

Overlay.

Uso.

---

# 18. Estados Visuales

Definir.

Hover.

Focus.

Pressed.

Disabled.

Selected.

Loading.

Error.

Empty.

Success.

---

# 19. Animaciones

Definir.

Duración.

Curvas.

Entrada.

Salida.

Hover.

Click.

Cambio de pantalla.

Movimiento.

---

# 20. Responsive

Definir.

Desktop.

Tablet.

Mobile.

Comportamiento.

Escalado.

Espaciado.

---

# 21. Accesibilidad

Definir.

Contraste.

Focus.

Teclado.

Lectores.

Mensajes.

---

# 22. Componentes Compartidos

La IA debe listar todos los componentes reutilizables.

Ejemplo.

Button

Card

Input

Modal

Dialog

Badge

Chip

Avatar

Tooltip

Toast

Spinner

Loader

Tabs

Accordion

Navbar

Sidebar

Footer

Header

Pagination

SearchBar

---

# 23. Componentes Específicos

Listar únicamente los propios del proyecto.

Ejemplo.

Board

Dice

PlayerCard

Inventory

GameLog

BattlePanel

CharacterCard

PokemonCard

Etc.

---

# 24. Consistencia

Todas las pantallas deben cumplir.

- Misma paleta.
- Misma tipografía.
- Mismo espaciado.
- Misma alineación.
- Misma altura de botones.
- Misma altura de inputs.
- Misma familia de iconos.
- Misma jerarquía visual.

---

# 25. Buenas Prácticas

La IA debe respetar.

- Evitar colores innecesarios.
- No mezclar estilos.
- No cambiar componentes entre pantallas.
- Mantener alineación consistente.
- Mantener espacios consistentes.
- Mantener tamaños consistentes.
- Evitar sobrecargar la interfaz.
- Priorizar claridad sobre decoración.

---

# 26. Checklist

Antes de implementar verificar.

- [ ] Paleta definida.
- [ ] Tipografía definida.
- [ ] Espaciados definidos.
- [ ] Componentes compartidos definidos.
- [ ] Estados visuales definidos.
- [ ] Responsive definido.
- [ ] Animaciones definidas.
- [ ] Accesibilidad considerada.
- [ ] Consistencia con architecture.md.
- [ ] Consistencia con layout.md.

---

# Restricciones para la IA

Durante este documento:

- No escribir HTML.
- No escribir CSS.
- No escribir SCSS.
- No escribir TypeScript.
- No crear componentes.
- No modificar la arquitectura.
- No cambiar las tecnologías definidas en el SDD.
- No inventar funcionalidades.

Este documento debe servir como referencia visual para toda la implementación del FrontEnd.
