# Ficha técnica · Sendeiro (borrador que debe verificarse)

- **Paquete:** `gal.sendeiro.rutas` (debug añade `.debug`).
- **versionCode / versionName:** `1` / `1.0`.
- **SDK mínimo / objetivo:** API 26 / API 35, según la configuración incluida; confirmar con el perfil de dispositivos de la UD1.
- **Capacidades:** pantalla táctil; ubicación opcional. La aplicación no exige GPS como requisito de instalación.
- **Permisos:** ubicación precisa/aproximada, solicitada solo al pulsar “Registrar ubicación”. Si se deniega, el resto sigue disponible; si se deniega permanentemente, se ofrece Ajustes.
- **Almacenamiento:** rutas en Room; preferencia de unidades y borrador en preferencias; sesión no incluida en backup. En producción, históricos/pendientes requieren sincronización remota segura.
- **Copia de seguridad:** desactivada en el manifiesto para no trasladar datos locales potencialmente sensibles sin una política explícita.
- **Dispositivo real probado:** PENDIENTE. No se ha comunicado marca, modelo ni versión de Android y no se afirma una prueba inexistente.
- **Tamaño APK debug / release:** PENDIENTE de compilar. No inventar tamaños; obtener con `Get-Item app/build/outputs/apk/{debug,release}/*.apk` en PowerShell.
- **Qué falta para ser producto:** backend/autenticación, sincronización, mapas y seguimiento GPS reales, adjuntos de foto/audio, catálogo de rutas oficial, pruebas en AVD pequeño/grande y móvil real, accesibilidad, pruebas automatizadas, firma release y evidencias.

## Firma release
Crear el almacén fuera del repositorio y guardar una copia segura. Configurar en `~/.gradle/gradle.properties` (Windows: `%USERPROFILE%\.gradle\gradle.properties`) las propiedades `SENDEIRO_STORE`, `SENDEIRO_PASS`, `SENDEIRO_ALIAS` y `SENDEIRO_KEY_PASS`. No introducir contraseñas en Git.
