# Checklist de Evaluación – Proyecto Práctico

Este documento recopila todos los criterios de evaluación del recuperatorio práctico, organizados por áreas y con especial énfasis en las condiciones de **desaprobación directa**.

Úsalo como **checklist vivo**: marca cada ítem como `[x]` solo cuando estés 100% seguro de que cumples con el requisito. Revisa el documento entero al menos 48 horas antes de la entrega para tener margen de corrección.

---

## 1. Consideraciones Generales de Backend, API y Juego

### 1.1. Compilación y ejecución
- [ ] El backend compila sin errores (`mvn clean install` exitoso).
- [ ] La aplicación arranca correctamente (sin excepciones bloqueantes en el log).
- [ ] No hay ciclos infinitos, deadlocks ni problemas que impidan levantar el servidor.

### 1.2. Testing obligatorio (backend)
- [ ] Existen tests escritos para el backend (no se evalúan tests de frontend).
- [ ] Todos los tests compilan correctamente.
- [ ] **No hay tests fallidos** (0 errores, 0 fallos).

### 1.3. Cobertura JaCoCo
- [ ] La cobertura mínima obligatoria es del **95%**.
- [ ] El umbral no ha sido modificado artificialmente a la baja.
- [ ] El plugin de JaCoCo está configurado para fallar el build si no se alcanza el 95%.

### 1.4. Reglas del juego (corazón funcional)
- [ ] **Vidas**: implementadas según el enunciado (número inicial, pérdida, regeneración, etc.).
- [ ] **Tablero**: construido correctamente con todas las casillas y dimensiones.
- [ ] **Zonas especiales**: funcionan (ej. trampas, bonificaciones, teletransportes, etc.).
- [ ] **Dados**: valores correctos, tiradas aleatorias o simuladas según corresponda.
- [ ] **Turnos**: avanzan de forma secuencial y respetan el orden de los jugadores.
- [ ] **Condiciones de victoria**: implementadas y detectables.
- [ ] **Desempates**: gestionados según lo especificado.
- [ ] **Segunda parte del trabajo**: integrada correctamente y funcional.

### 1.5. Arquitectura backend
- [ ] Separación clara de capas:
    - [ ] **Controller** – expone endpoints, valida DTOs.
    - [ ] **Service** – contiene la lógica de negocio (no debe estar en el controller).
    - [ ] **Domain/Entity** – modela las entidades del juego.
    - [ ] **Repository** – acceso a datos (JPA/Spring Data).
- [ ] Los DTOs se usan para la comunicación con el exterior (no se exponen entidades completas sin control).

### 1.6. Patrones de diseño
- [ ] Se valora el uso adecuado de: **Strategy**, **Factory**, **State**, **Registry**, **Chain of Responsibility**, etc.
- [ ] Los patrones están bien aplicados y realmente aportan a la solución (no son forzados).

### 1.7. API y validaciones
- [ ] Validaciones de DTOs mediante `@Valid` / `@Validated`.
- [ ] Manejo correcto de errores con respuestas HTTP adecuadas (400, 401, 403, 404, 500, etc.).
- [ ] Autenticación y autorización implementadas.
- [ ] **Ownership** de partidas: un usuario solo puede modificar sus propias partidas.

### 1.8. Persistencia real
- [ ] Las entidades del juego se persisten en una base de datos real (no en memoria, no con datos simulados).
- [ ] El estado del juego se recupera correctamente entre reinicios de la aplicación.

### 1.9. Seguridad
- [ ] **No** hay contraseñas en texto plano (deben estar cifradas/hasheadas).
- [ ] **No** se exponen entidades completas en respuestas REST (usar DTOs).
- [ ] **No** se devuelven passwords ni información sensible.
- [ ] La autenticación no es falsificable (JWT bien configurado, secretos no expuestos).
- [ ] Los endpoints críticos están protegidos (no se puede acceder sin token).

---

## 2. Consideraciones de Frontend

### 2.1. Interacción con el sistema
- [ ] El usuario puede interactuar con la aplicación sin bloqueos.
- [ ] La interfaz refleja de forma clara el estado y el funcionamiento esperado.

### 2.2. Pantallas principales
- [ ] Todas las pantallas requeridas están desarrolladas.
- [ ] La navegación entre pantallas es coherente e intuitiva.
- [ ] El usuario puede realizar todas las acciones necesarias sin quedar atascado.

### 2.3. Comunicación con backend
- [ ] El frontend consume **datos reales** desde el backend (no datos simulados o fijos).
- [ ] Los servicios Angular están correctamente inyectados y configurados.
- [ ] Las llamadas HTTP manejan errores y tiempos de espera.

### 2.4. Formularios y validaciones
- [ ] Validaciones básicas en los formularios (campos obligatorios, formatos, etc.).
- [ ] No se permiten envíos con datos incompletos o inválidos.
- [ ] Los mensajes de error son claros y amigables para el usuario.

### 2.5. Estados del sistema
- [ ] La UI muestra correctamente:
    - [ ] Información cargada (ej. datos de partida).
    - [ ] Resultados de acciones (ej. tirada de dados, movimiento).
    - [ ] Errores (con mensajes entendibles).
    - [ ] Confirmaciones (ej. partida creada, unión a partida).
    - [ ] Cambios de estado después de operar (actualización reactiva).

### 2.6. Organización Angular
- [ ] Separación en **componentes** (cada uno con su responsabilidad).
- [ ] **Servicios** para la lógica de negocio y comunicación HTTP.
- [ ] **Modelos/Interfaces** para tipado de datos.
- [ ] **Rutas** definidas en `app-routing.module`.
- [ ] **Guards** si se requiere protección de rutas.
- [ ] **Módulos** organizados (o estructura equivalente a la vista en clase).
- [ ] La lógica no está toda mezclada en un único componente.

### 2.7. Rutas y sesión
- [ ] Uso correcto de rutas y navegación.
- [ ] Manejo de sesión o token (almacenamiento en localStorage/sessionStorage o similar).
- [ ] Persistencia mínima de autenticación (el usuario no tiene que loguearse en cada acción).

### 2.8. Presentación general
- [ ] Diseño prolijo y coherente.
- [ ] Textos claros y sin faltas de ortografía.
- [ ] Botones con etiquetas entendibles (ej. "Guardar", "Tirada", "Unirse").
- [ ] **Sin errores visibles en la consola del navegador** (excepciones, 404, CORS, etc.).
- [ ] Experiencia de uso razonable y fluida.

---

## 3. Estructura Obligatoria del Proyecto

### 3.1. Estructura Backend
- [ ] Organización coherente de paquetes:
    - `controller`
    - `service`
    - `domain/entity`
    - `repository`
    - `dto`
    - `config`
    - `exception`
    - `util` (si aplica)
- [ ] La estructura respeta el scaffolding trabajado en clase.

### 3.2. Estructura Frontend
- [ ] Estructura Angular estándar:
    - `src/app/components`
    - `src/app/services`
    - `src/app/models/interfaces`
    - `src/app/guards`
    - `src/app/modules` (si se usa modularización)
- [ ] No se mezclan responsabilidades en un solo sitio.

> ⚠️ **Importante**: no se considerará correcta una entrega donde toda la lógica quede mezclada, sin separación de responsabilidades o con una estructura incompatible con la forma de trabajo indicada en clase.

---

## 4. Condiciones de Desaprobación Directa

**Estos casos implican la desaprobación automática del recuperatorio práctico.** Revisa cada uno con lupa.

### ❌ No se recibió entrega
- Desaprueba directo. No hay nada que corregir.

### ❌ El proyecto no compila o no ejecuta
- Maven falla (`mvn clean install`).
- Los tests de backend no compilan.
- Hay ciclos infinitos que bloquean la suite de tests.
- La aplicación no levanta (puerto ocupado, excepción no manejada, etc.).

### ❌ No hay tests escritos en backend
- Desaprueba directo, especialmente si la consigna exigía cobertura.

### ❌ No cumple el 95% de cobertura obligatorio en backend
- **Cobertura ≤ 94.9%** → desaprueba. No es "casi"; es por debajo del mínimo.

### ❌ Hay tests fallidos en backend
- Si hay **uno o más tests fallando**, el recuperatorio no debería aprobarse.

### ❌ El backend no persiste las entidades del juego
- El juego debe guardar y recuperar estado de una base de datos real (no en memoria).

### ❌ El juego no implementa reglas centrales del enunciado
- Vidas incorrectas, tablero mal construido, zonas especiales que no se usan, dados con valores erróneos, condiciones de victoria incompletas o desempates faltantes.

### ❌ La aplicación no funciona end-to-end
- Errores de CORS que impiden la comunicación.
- Endpoints que no permiten jugar.
- Turnos que no avanzan.
- Creación de partida rota.

### ❌ Se bajó artificialmente el umbral de cobertura
- Modificar el `pom.xml` para reducir el mínimo exigido es desaprobación directa.

### ❌ Se usó una herramienta distinta a la exigida
- Ejemplo: se pidió Maven y se entregó Gradle, junto con ausencia de tests o estructura incompatible.

### ❌ No se respeta la estructura base indicada en clase
- Si la estructura del frontend o backend es incompatible con el scaffolding trabajado en clase y eso impide corregir, ejecutar o entender la solución, puede implicar desaprobación directa.

---

