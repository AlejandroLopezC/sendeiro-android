# Decisiones de la unidad · Sendeiro

## Actividad 1 — Orientación y estado
No se bloquea la orientación: el formulario debe adaptarse al uso real y a accesibilidad. Renunciamos a evitar la recreación mediante una restricción artificial; guardamos nombre y distancia en `onSaveInstanceState`/`Bundle` para cambios de configuración. El Bundle sirve para estado temporal, no como almacenamiento duradero. En React Native el estado de pantalla irá en estado de componente/estado de navegación; preferencias sencillas en almacenamiento local y rutas/partes en una base de datos persistente.

## Actividad 2 — Formulario y validación
Se utiliza un formulario con `ScrollView` y borrador local para que el voluntario no pierda lo escrito si los once campos no caben. El botón de guardar permanece activo y explica qué campo falla; se renuncia a la simplicidad de desactivarlo porque, con un formulario largo, el usuario necesita saber por qué no puede guardar. Se valida al escribir, al perder foco y al guardar; el borrador limita longitudes y la operación final vuelve a validar.

## Actividad 3 — Listado y borrado
Se usa `RecyclerView` con `ViewHolder`, y se asigna siempre `VISIBLE` o `GONE` a la etiqueta de cierre para impedir estado residual. Se elige pulsación larga con confirmación: requiere descubrir el gesto y es menos evidente que un botón, pero evita saturar cada fila y reduce el riesgo de borrado accidental. No se usa deslizamiento porque añade conflicto con el scroll y exige deshacer.

## Actividad 4 — Persistencia, copia y permiso
Las rutas y partes estructurados van a Room; el ajuste de unidades y el borrador temporal van a preferencias. La copia automática está desactivada para no trasladar secretos ligados al dispositivo; un token de sesión no se restaura y se renueva autenticándose. Se renuncia a una restauración automática completa y se requiere volver a descargar rutas/autenticar la cuenta. La ubicación se solicita solo al pulsar la función, con alternativa funcional si se deniega y acceso a Ajustes si se deniega permanentemente.
