# Actividad 4 · Tabla de decisiones de almacén

| Dato | Almacén elegido | Si cambia de móvil |
|---|---|---|
| Ruta que está siguiendo ahora | Estado temporal/Bundle; la traza puede reconstruirse desde la ruta seleccionada | No se conserva necesariamente tras una instalación nueva; se vuelve a seleccionar. |
| Parte a medio escribir | Preferencias locales como borrador limitado; en producción, borrador en Room | No se restaura con la copia automática desactivada; debe exportarse/sincronizarse si se necesita recuperar. |
| Partes pendientes de enviar, con fotos | Room más ficheros privados de la app; sincronización con servidor recomendada | Con backup desactivado no se trasladan; producción debe sincronizar de forma segura antes de cambiar de móvil. |
| Histórico de partes enviados | Servidor como fuente principal y caché Room | Se recupera al iniciar sesión y sincronizar. |
| 140 rutas descargadas | Room para metadatos y almacenamiento privado para GPX/mapas | Se vuelven a descargar o se migran mediante sincronización. |
| Unidades: km o millas | `SharedPreferences` | Preferencia local; si no se migra, vuelve al valor predeterminado. |
| Token de sesión | Almacenamiento seguro ligado al dispositivo; nunca copiarlo en backup | Se invalida/renueva iniciando sesión de nuevo. |

La copia automática se desactiva con `android:allowBackup="false"` para evitar restaurar secretos ligados al dispositivo. Una app de producción necesita definir una estrategia de sincronización y recuperación de borradores, no limitarse a desactivar la copia.
