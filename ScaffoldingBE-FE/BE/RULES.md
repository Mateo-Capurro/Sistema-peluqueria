# Reglas de Desarrollo Backend (Buenas Prácticas)
Este documento establece las directrices fundamentales para mantener un código claro, flexible, escalable y mantenible.

## Principios de Diseño (SOLID)
Se deben aplicar rigurosamente los principios SOLID para asegurar el desacoplamiento:
- **SRP (Single Responsibility):** Cada clase debe tener una única razón para cambiar.
- **OCP (Open/Closed):** Código abierto a la extensión pero cerrado a la modificación.
- **LSP (Liskov Substitution):** Las subclases deben ser sustituibles por sus clases base.
- **ISP (Interface Segregation):** Evitar interfaces "gordas", preferir interfaces específicas.
- **DIP (Dependency Inversion):** Depender de abstracciones (interfaces), no de clases concretas.

## Convenciones de Codificación
- **Nomenclatura:**
  - `PascalCase`: Clases, Interfaces, Enums.
  - `camelCase`: Métodos, variables, parámetros.
  - `UPPER_CASE`: Constantes.
- **Modificadores de Acceso:** Aplicar el principio de menor privilegio (`private` por defecto, usar `protected` o `public` solo cuando sea estrictamente necesario).
- **Inmutabilidad:** Preferir objetos inmutables para configuraciones y estados compartidos (Thread-Safety).

## Calidad y Testing
- **Pruebas Unitarias (JUnit):** Todo componente crítico debe tener su suite de tests.
- **Aislamiento (Mockito):** Usar mocks para dependencias externas (BD, APIs) para garantizar pruebas unitarias rápidas y enfocadas.
- **Naming de Tests:** Seguir el patrón `debe[Comportamiento]Cuando[Escenario]`.

## Gestión de Proyecto (Maven)
- **POM.xml:** Mantener el archivo limpio, evitando dependencias innecesarias.
- **Gestión de Versiones:** Utilizar versiones de librerías estables y gestionarlas mediante propiedades en el POM para evitar conflictos.
- **Ciclo de Vida:** Respetar las fases estándar (`clean`, `compile`, `test`, `package`, `install`).

## Arquitectura y APIs
- **REST:** Utilizar la semántica correcta de verbos HTTP:
  - `GET`: Recuperar datos.
  - `POST`: Crear recursos.
  - `PUT`: Reemplazar recursos.
  - `PATCH`: Modificación parcial.
  - `DELETE`: Eliminar.
- **Tratamiento de Excepciones:** No devolver siempre 200 OK en caso de errores. Usar los códigos de estado HTTP adecuados (4xx para cliente, 5xx para servidor).

## Diseño de APIs RESTful
### Recursos y Naming: 
Utilizar sustantivos para representar conjuntos de datos y evitar el uso de verbos en la URL.
### Documentación:
- Cada endpoint debe incluir: Método HTTP, descripción, parámetros (path, query, body), ejemplos de request/response (JSON) y códigos de estado HTTP esperados.  
- Mantener la documentación actualizada en el mismo repositorio del proyecto.  
- Se recomienda el uso de OpenAPI (Swagger) para generar documentación navegable y Postman para colecciones de endpoints.
### Versiones: 
- Incluir notas de cambio (changelog) entre versiones para identificar modificaciones.
### Manejo de datos: 
Implementar paginación, ordenamiento y filtros cuando la colección de datos sea extensa. 

## Patrones de Diseño (GoF)
### Criterio de Aplicación: 
No aplicar patrones por obligación. 
El objetivo es resolver problemas específicos de diseño (creacionales, estructurales o de comportamiento) para hacer el código más claro y flexible.  
#### Problemas comunes y soluciones sugeridas:
- Exceso de parámetros opcionales en constructores: Usar Builder.
- Interfaz externa incompatible: Usar Adapter para traducir interfaces.
- Objetos casi iguales construidos desde cero: Usar Prototype.
- Flexibilidad en algoritmos: Usar Strategy para definir familias de algoritmos intercambiables.
- Esqueleto de algoritmo en operación: Usar Template Method para delegar pasos a subclases.
- Acoplamiento débil entre objetos: Usar Mediator.
  
## Spring Boot
### Calidad de Código:
Mantener estructuras de código limpias sin dependencias circulares entre paquetes.  
Proveer javadoc significativo, actual y preciso.  
### Gestión de Errores: 
Implementar mecanismos robustos, incluyendo registro/monitoreo, respuestas con códigos HTTP significativos y estrategias de reintento/recuperación.  
### Patrones: 
Utilizar el patrón BFF (Back-end-for-frontend) para optimizar el soporte según el tipo de interfaz de usuario.  
### Estándares en Spring
Utilizar las anotaciones correspondientes según la capa:
- @RestController: Controladores RESTful.
- @Repository: Componentes de acceso a datos.
Asegurar que los JavaBeans sigan las convenciones (propiedades privadas, constructor público sin argumentos, serializable).
