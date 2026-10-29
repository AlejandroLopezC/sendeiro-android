# Lista de comprobación frente al cuaderno PMDM-UD2

Este ZIP es un proyecto inicial funcionalmente estructurado, no una declaración de que las pruebas se hayan ejecutado. Antes de considerarlo entregable, completa y adjunta:

- [ ] Compilar con `gradlew.bat assembleDebug` en Windows y corregir cualquier error real de Gradle/SDK.
- [ ] Instalar y probar en los dos AVD (pequeño y grande) y en móvil físico.
- [ ] Capturas reales de los seis casos de ciclo de vida y de los campos antes/después del giro.
- [ ] Boceto de la pantalla (foto) y capturas en los dos AVD.
- [ ] Probar validación al escribir, al perder foco, al guardar y al restaurar un borrador inválido.
- [ ] Vídeo del fallo de reciclado con `else` retirado y vídeo tras arreglarlo.
- [ ] Captura del estado vacío y recuento real de llamadas de reciclado durante scroll.
- [ ] Completar la ficha técnica con móvil real y tamaños APK medidos.
- [ ] Generar APK release firmado con tu propio almacén de claves fuera del repositorio e instalarlo en móvil real.
- [ ] Captura/vídeo del permiso concedido, denegado y denegado permanentemente.
- [ ] Crear etiquetas Git `pmdm-ud2-a1`, `pmdm-ud2-a2`, `pmdm-ud2-a3`, `pmdm-ud2-a4` y `pmdm-ud2` en el repositorio real; incluir el enlace a la etiqueta final y los tres vídeos en el PDF de entrega.
- [ ] Unir las cinco piezas documentales en un único PDF en el orden exacto del cuaderno.

No se incluyen APK ni capturas/vídeos inventados. El entorno de generación de este ZIP no tiene Android SDK/Gradle instalado para compilarlo aquí.


Actualización del borrador: el código inicializa 140 entradas de demostración si la base está vacía. Son datos sintéticos, no el catálogo oficial de la federación. El Spinner de dificultad se inicializa desde `strings.xml` y el identificador de recurso de estado vacío usa `vacio_rutas`. Estos cambios no sustituyen una compilación ni una prueba real.
