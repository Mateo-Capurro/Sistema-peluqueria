Este documento establece las directrices para el desarrollo de aplicaciones frontend robustas, escalables y mantenibles bajo los estándares actuales.

1. Arquitectura de Componentes
Standalone Components: Priorizar siempre el uso de Standalone Components. Evitar el uso de NgModule a menos que sea estrictamente necesario por compatibilidad con librerías de terceros antiguas.

Signals: Adoptar la reactividad basada en Signals para la detección de cambios.

Usar signal() para estado local.

Usar computed() para valores derivados (lectura reactiva y optimizada).

Evitar el uso de Zone.js cuando sea posible en futuras migraciones a Zoneless.

Control Flow Nativo: Utilizar la sintaxis de flujo de control integrada (@if, @else, @for, @switch) en lugar de las antiguas directivas estructurales (*ngIf, *ngFor).

Regla obligatoria: Al usar @for, incluir siempre la cláusula @track para optimizar el renderizado y evitar errores de compilación.

2. Comunicación y Datos
Comunicación Padre-Hijo:

Entrada: Usar decoradores @Input() o Signal Inputs (input()).

Salida: Usar @Output() con EventEmitter.

Comunicación entre componentes:

Utilizar servicios inyectables (Singleton con providedIn: 'root') para compartir estado entre componentes que no tienen una relación directa.

Routing:

Implementar Lazy Loading para las rutas de la aplicación.
Las rutas y su definición se deben ubicar dentro de app.routes.ts.
Utilizar la anotacion <router-outler> dentro de app.component.html para el ruteo de componentes
Usar withComponentInputBinding() para capturar parámetros de ruta dinámicos directamente en las propiedades del componente, evitando la inyección manual de ActivatedRoute.

3. Inyección de Dependencias
Uso de inject(): Priorizar la función inject() sobre la inyección en el constructor. Esto facilita la reutilización de lógica fuera de clases y simplifica la estructura de los componentes.

Alcance (Scope):

Definir servicios en el nivel superior (providedIn: 'root') para comportamientos globales (Singleton).

Definir servicios en providers: [] de un componente solo cuando se requiera una instancia específica y privada para dicho componente y su subárbol.

4. Estándares de Código y Calidad
Pipes:

Utilizar Pipes para la transformación de datos en plantillas.

Si se crea un pipe personalizado, debe implementarse la interfaz PipeTransform.

Asegurar siempre su importación en el array imports del componente standalone.

Manejo de Errores:

Implementar interceptores globales para manejar errores HTTP de forma centralizada.

Asegurar que la interfaz sea accesible, utilizando las recomendaciones de Angular Aria.

Testing: Seguir las mejoras críticas en herramientas de testing integradas en las últimas versiones para asegurar la estabilidad en producción.

5. Documentación
Mantener la documentación de componentes y servicios actualizada utilizando JSDoc/TSDoc para facilitar la lectura del código por otros desarrolladores.

Seguir las convenciones de nomenclatura y estructura de archivos definidas por Angular CLI.
