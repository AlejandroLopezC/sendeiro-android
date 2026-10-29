# Actividad 3 · Estados del listado

| Estado | Qué ve el usuario | Implementación / evidencia |
|---|---|---|
| Cargando | “Cargando rutas…” y progreso visible. | `ProgressBar` y etiqueta de estado. |
| Con datos | Número de rutas y filas con nombre, distancia, dificultad y estado. | `RecyclerView` alimentado por `Flow` de Room. |
| Vacío | “Todavía no hay rutas guardadas. Añade una ruta con el formulario para verla aquí.” | Mensaje accionable; borrar todas las rutas permite comprobarlo. |
| Error | “No se han podido cargar las rutas. Comprueba e inténtalo de nuevo.” | Captura de error del `Flow`; para producción debe añadirse acción de reintento. |

El adaptador registra `onCreateViewHolder` y `onBindViewHolder` con el tag `RECICLADO`. El proyecto incluye 30 rutas de demostración la primera vez para probar desplazamiento; para demostrar el fallo, se debe grabar el estado con el `else` temporalmente retirado y luego la versión corregida. Esa evidencia está pendiente de grabación real.
