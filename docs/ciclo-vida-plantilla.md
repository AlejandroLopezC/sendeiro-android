# Actividad 1 · Registro de ciclo de vida (completar con pruebas reales)

**Importante:** las secuencias siguientes son predicciones técnicas orientativas, no resultados observados. Sustituye la columna observada y adjunta capturas reales de `adb logcat -s CICLO:D` después de ejecutar cada prueba.

| Caso | Secuencia esperada orientativa | Secuencia observada / evidencia |
|---|---|---|
| Girar el móvil | `onPause → onSaveInstanceState → onStop → onDestroy → onCreate → onStart → onRestoreInstanceState → onResume` (el orden exacto de guardado/restauración puede variar por versión). | PENDIENTE: ejecutar y capturar |
| Cambiar idioma del sistema | Recreación habitual: pausa, guardado, parada, destrucción y creación de nuevo. | PENDIENTE: ejecutar y capturar |
| Pulsar atrás | `onPause → onStop → onDestroy`. | PENDIENTE: ejecutar y capturar |
| Activar modo oscuro | Si cambia configuración y la Activity no lo gestiona: recreación. | PENDIENTE: ejecutar y capturar |
| Cambiar tamaño de letra | Recreación habitual por cambio de configuración. | PENDIENTE: ejecutar y capturar |
| Matar proceso con `adb shell am kill` | No hay callbacks de cierre garantizados al matar el proceso; al volver, la restauración depende del estado guardado y del modo de relanzamiento. | PENDIENTE: ejecutar y capturar |

Comandos: `adb logcat -c` y después `adb logcat -s CICLO:D`. Para probar giro, usa controles del emulador; no se deben registrar trazas inventadas como observadas.
