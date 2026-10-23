# Entorno (entorno-android)

App Android de reportes ciudadanos — Jetpack Compose + Kotlin.
Universidad del Quindío · Construcción de Aplicaciones Móviles.

## Estado: Fase 2 (funcionalidades básicas)

Las 20 pantallas del prototipo de la Fase 1 están implementadas, conectadas entre sí y
funcionales. **Los datos viven solo en memoria**: al cerrar la app se reinician con los datos de
demostración (la persistencia es de la Fase 3). No hace falta `google-services.json` para compilar.

### Cuentas de demostración (contraseña `123456`)

| Correo | Rol |
|---|---|
| `juan.cayon@uqvirtual.edu.co` | Ciudadano (con reportes, comentarios y notificaciones) |
| `johanc.garciag@uqvirtual.edu.co` | Ciudadano |
| `moderador@entorno.co` | Moderador (ve la pestaña **Moderación** y puede verificar/rechazar) |

También se puede crear una cuenta nueva desde **Crear cuenta**.

### Mapa de navegación

- **Sin sesión:** Bienvenida → Iniciar sesión / Crear cuenta → Recuperar contraseña · Términos · Privacidad.
- **Con sesión** (barra inferior): **Inicio** · **Mapa** · **Mis reportes** · **Perfil** (+ **Moderación** para moderadores).
  - Inicio: buscador y filtros por categoría, notificaciones (campana), crear reporte (con selector de ubicación en el mapa), detalle del reporte, comentarios (con respuestas).
  - Mapa: marcadores por categoría, búsqueda de barrio, radio de alertas, mapa de calor.
  - Perfil: editar perfil, configuración (cambiar contraseña, notificaciones, radio de alertas, modo oscuro, cerrar sesión), insignias y nivel, héroes comunitarios, estadísticas.
  - Moderación: cola de publicaciones pendientes → revisar → verificar o rechazar con motivo.
- Cerrar sesión o eliminar la cuenta devuelve solo a la Bienvenida (la navegación depende de la sesión).

### Qué es "en memoria"

`data/repository/memory/` contiene una implementación en memoria de cada repositorio del dominio
(`InMemoryAuthRepository`, `InMemoryReportRepository`, …) sobre una única `MemoryDatabase` con datos
de demostración (`MemorySeed`). Las reglas de puntos, niveles, insignias y notificaciones
(`MemoryRules`) se aplican ahí en cada acción. `di/RepositoryModule` enlaza las interfaces del dominio
con estas implementaciones.

El mapa es **esquemático** (calles, parques y río dibujados en Compose, con zoom y desplazamiento):
no usa un SDK de mapas ni claves de API.

### Pasar a Firebase (Fase 3)

Las implementaciones de Firebase/DataStore/Cloudinary siguen en `data/repository/*Impl.kt`.
Basta con volver a enlazar cada interfaz en `di/RepositoryModule.kt` y colocar el
`google-services.json` en `app/`. Las pantallas y ViewModels no cambian: solo conocen las interfaces
del dominio.

### Pruebas

```
./gradlew test
```

`InMemoryRepositoriesTest` cubre autenticación, reportes, moderación, votos, comentarios,
notificaciones, ajustes, perfil y ranking.

### Credenciales y archivos que no se versionan

`app/google-services.json` (solo necesario desde la Fase 3).
