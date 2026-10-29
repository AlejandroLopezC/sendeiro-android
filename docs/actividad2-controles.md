# Actividad 2 · Tabla de decisiones de control

| Dato | Control | Tipo de entrada / equivalente | Justificación |
|---|---|---|---|
| Nombre de ruta | `EditText` | `textCapSentences`, máximo 80 caracteres | Texto corto y editable. |
| Distancia en kilómetros | `EditText` | `numberDecimal`, validación numérica adicional | El teclado no impide pegar texto inválido; se normaliza coma decimal y se valida el valor. |
| Dificultad | `Spinner` | Lista fija de tres valores | Solo permite opciones definidas. |
| ¿Circular? | `CheckBox` | Booleano | Elección independiente sí/no. |
| Tramo de una lista de 140 | `AutoCompleteTextView` con búsqueda (recomendación para producción) | Texto con filtro de catálogo | Un Spinner de 140 opciones es incómodo; el usuario debe poder filtrar. La pantalla demostrativa actual aún no implementa catálogo de tramos. |
| Descripción | `EditText` | `textMultiLine` | Texto libre de varias líneas. |

Validación: al escribir, al perder foco y al pulsar Guardar. Se debe verificar también el borrador y las cadenas `12,5 km`, `abc`, negativos y valores excesivamente largos en un emulador.
