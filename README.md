# Entorno (entorno-android)

App Android de reportes ciudadanos — Jetpack Compose + Kotlin.
Universidad del Quindío · Construcción de Aplicaciones Móviles.

## Estado

Las pantallas del prototipo están implementadas y conectadas a Firebase (Auth, Firestore),
Cloudinary (imágenes), GeoFire (consultas por ubicación), DataStore (sesión y ajustes) y Mapbox
(mapas). La detección de reportes duplicados usa Gemini. Las claves van en `local.properties`
(ver el final de este archivo).

### Mapa de navegación

- **Sin sesión:** Bienvenida → Iniciar sesión / Crear cuenta → Recuperar contraseña · Términos · Privacidad.
- **Con sesión** (barra inferior): **Inicio** · **Mapa** · **Mis reportes** · **Perfil** (+ **Moderación** para moderadores).
  - Inicio: buscador y filtros por categoría, notificaciones (campana), crear reporte (con selector de ubicación en el mapa), detalle del reporte, comentarios (con respuestas).
  - Mapa: marcadores por categoría, búsqueda de barrio, radio de alertas, mapa de calor.
  - Perfil: editar perfil, configuración (cambiar contraseña, notificaciones, radio de alertas, modo oscuro, cerrar sesión), insignias y nivel, héroes comunitarios, estadísticas.
  - Moderación: cola de publicaciones pendientes → revisar → verificar o rechazar con motivo.
- Cerrar sesión o eliminar la cuenta devuelve solo a la Bienvenida (la navegación depende de la sesión).

### Pruebas

```
./gradlew test
```

Las pruebas unitarias están en `app/src/test`. `data/repository/memory/` (en `test/`) es una
implementación **falsa en memoria** de los repositorios, usada como *fake* en
`InMemoryRepositoriesTest`; la app real no la usa. También hay pruebas de `Severity`, `UserLevel`,
`Geo` y del peso del mapa de calor (`heatWeight`).

### Credenciales y archivos que no se versionan

- `app/google-services.json` (Firebase).
- `local.properties`: `MAPBOX_ACCESS_TOKEN`, `GEMINI_API_KEY` y las claves de Cloudinary.
