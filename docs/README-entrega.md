# Sendeiro Android · PMDM-UD2

Paquete de trabajo para las cuatro actividades. Abre la carpeta en VS Code. Requisitos: JDK 17, Android SDK API 35, Platform Tools y un dispositivo/emulador. Configura `local.properties` localmente si Gradle no detecta el SDK (no se versiona).

En Windows, cuando tengas Gradle Wrapper completo en el proyecto: `gradlew.bat assembleDebug`, `gradlew.bat installDebug`.

**Importante:** no se ha podido compilar este proyecto en el entorno de creación porque no tiene Android SDK ni Gradle. La firma release requiere tu almacén de claves, que no se incluye por seguridad. Consulta `entrega-pendiente.md` antes de entregar.
