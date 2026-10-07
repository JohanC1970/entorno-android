# Entorno — app Android de reportes ciudadanos

Proyecto final de Construcción de Aplicaciones Móviles (Universidad del Quindío).
Kotlin + Jetpack Compose + Material 3. Paquete: `co.edu.uniquindio.entorno`
Equipo: Juan Esteban Cayón, Johan Camilo García, Juan Andrés Pérez.

## Estado actual

Las capas de **dominio y datos ya están construidas** (PR #3) con Firebase
Firestore, Firebase Auth, Cloudinary para imágenes, GeoFire para consultas
geoespaciales y DataStore para la sesión. Hilt está configurado como inyector
de dependencias.

Lo que falta es la **capa de presentación**: pantallas Compose, ViewModels y
navegación. Ese es el trabajo en curso.

NO reimplementar modelos, repositorios ni fuentes de datos: ya existen.
Antes de crear cualquier clase de dominio, revisar `domain/model/`.

## Pendientes en dominio y datos (para Johan)

- **Comentarios anidados:** `Comment` ya tiene `parentCommentId: String? = null`
  (null = comentario raíz; con valor = respuesta, un solo nivel; una respuesta a
  una respuesta se cuelga del comentario raíz). Falta persistirlo: agregarlo en
  `CommentDto`, en `Mappers.kt` (`toDomain` y `toDto`) y en `CommentRepositoryImpl`.
  Hasta entonces el campo se pierde al guardar.
- **Responder desde la UI:** cuando eso esté, `onSend` de `CommentsScreen` pasa de
  `(String) -> Unit` a `(String, parentCommentId: String?) -> Unit`, y la lista
  agrupa las respuestas bajo su comentario raíz (pendiente en presentación).
- **Rol del autor:** `Comment` no guarda el rol ni el nivel del autor, por eso
  `CommentsScreen` recibe `moderatorIds`. Ver el `TODO` en `CommentItem.kt`.

## Estructura de paquetes

```
co.edu.uniquindio.entorno/
├── core/
│   ├── component/      composables reutilizables (ReportCard, CategoryChip, StatusPill)
│   ├── navigation/     NavHost y rutas
│   ├── theme/          Color.kt, Theme.kt, Type.kt
│   └── util/           RequestResult, Validators, ErrorMessages, Extensions
├── data/
│   ├── model/          DTOs de Firestore + Mappers
│   ├── remote/         fuentes de datos (Firebase, Cloudinary)
│   └── repository/     implementaciones de los repositorios
├── di/                 FirebaseModule, RepositoryModule (Hilt)
├── domain/
│   ├── model/          modelos y enums del dominio
│   └── repository/     interfaces de repositorio
├── features/           una carpeta por pantalla: XxxScreen.kt + XxxViewModel.kt
└── MainActivity.kt
```

## Idioma de los nombres

- **Código en inglés**: clases, propiedades, funciones, enums, paquetes.
  El dominio ya está escrito así (`Report`, `ReportCategory`, `importantCount`).
- **Textos que ve el usuario, en español**: literales de UI y la propiedad
  `label` de los enums.

No traducir ni renombrar lo que ya existe.

## Modelos del dominio (ya existentes)

- `Report(id, title, description, category, location, status, imageUrls,
  ownerId, ownerName, date, rejectionReason, importantCount, commentCount)`
  con `severity` calculada desde `importantCount`
- `ReportCategory`: SECURITY, MEDICAL, INFRASTRUCTURE, PETS, COMMUNITY — cada
  uno con `label` en español
- `ReportStatus`: PENDING, VERIFIED, REJECTED, RESOLVED (cuatro, no cinco)
- `Severity`: LOW, MEDIUM, HIGH, CRITICAL — derivada de los votos
- `User(id, name, email, city, address, location, phoneNumber,
  profilePictureUrl, role, points, badges, createdAt)` con `level` derivado
- `UserRole`: USER, MODERATOR
- `UserLevel`: NOVICE, COLLABORATOR, GUARDIAN, COMMUNITY_HERO — con `label`
- `Badge`, `PointsRule`, `Comment`, `Location`, `AppNotification`, `UserSession`

### Cómo se muestran los estados en la UI

`PENDING` se muestra distinto según quién mira:

- Al ciudadano: **"En verificación"**
- Al moderador: **"Pendiente"**

`ReportStatus` no tiene propiedad `label`. La traducción a texto y a color vive
en `core/theme` o en `core/component`, nunca repetida dentro de cada pantalla.

`Severity` tiene cuatro niveles pero los mockups muestran tres. En la UI,
HIGH y CRITICAL se presentan ambos como "Alta".

## Arquitectura de presentación

- Cada pantalla: `XxxScreen.kt` + `XxxViewModel.kt` en `features/<feature>/`
- Estado: `data class XxxUiState` inmutable; `MutableStateFlow` privado expuesto
  con `asStateFlow()`; cambios con `_uiState.update { it.copy(...) }`
- ViewModels con `@HiltViewModel` y `@Inject constructor(...)` recibiendo las
  **interfaces** de `domain/repository/`, nunca las implementaciones
- En los composables se obtienen con `hiltViewModel()`
- Flujo de datos unidireccional: el estado baja como parámetro, los eventos
  suben como lambdas (`onClick`, `onTitleChange`)

### Result vs RequestResult

Son dos cosas distintas y no se mezclan:

- Los repositorios devuelven `Result<T>` de Kotlin y `Flow<T>` para lo observable
- `RequestResult` (`core/util/`) describe el **estado de la pantalla**:
  Loading / Success / Failure

El ViewModel traduce de uno a otro con `fold`. Las pantallas nunca ven `Result<T>`.

## Reglas de Compose

- Los composables de `core/component/` son **sin estado**: reciben datos y
  lambdas, nunca leen el ViewModel por dentro
- Colores y tipografías **siempre** desde `MaterialTheme.colorScheme` y
  `MaterialTheme.typography`. Nunca `Color(0xFF...)` fuera de `core/theme`
- `dynamicColor = false` en `EntornoTheme`: con `true`, Android 12+ reemplaza
  nuestra paleta por los colores del fondo de pantalla del usuario
- Cada `XxxScreen` lleva su `@Preview` con `showBackground = true`
- Toda pantalla que pueda crecer usa `LazyColumn`, no `Column` + `verticalScroll`
- Los previews no pueden usar `hiltViewModel()`: separar un composable interno
  sin estado que reciba el `UiState` como parámetro, y previsualizar ese

## Reglas de Kotlin (criterios de calificación del curso)

- `val` sobre `var`; nunca usar `!!`
- Estados, categorías, roles y niveles como `enum class` o `sealed class`, nunca `String`
- `if` / `when` / `try` como expresiones en vez de asignaciones intermedias

## Pantallas (14 mockups aprobados en Fase 1)

Splash, Login/Registro, Home Feed, Mapa de reportes, Detalle del reporte,
Comentarios, Crear reporte, Mis reportes, Notificaciones, Perfil,
Héroes comunitarios, Estadísticas, Configuración, y **Mapa de calor**
(requisito adicional; escala secuencial de un solo tono y filtro por franja horaria).

## Diseño

- Primario `#2F3BA2` · onPrimary `#FFFFFF` · primaryContainer `#E2E4FF`
- Fondo `#FAFAFC` · tarjetas `#FFFFFF` · superficie alterna `#F1F2F7`
- Texto `#1A1B25` / `#5A5C6E` · divisores `#DFE1EA`
- Titulares: Archivo · Interfaz: Public Sans
- Los colores de categoría, estado y severidad ya están en `core/theme/Color.kt`
- Las 5 categorías nunca se distinguen solo por color: siempre icono + etiqueta

## Reparto de trabajo

- **Presentación** (`features/`, `core/component/`, `core/navigation/`):
  Juan Esteban
- **Dominio y datos** (`domain/`, `data/`, `di/`): Johan

Quien trabaje en presentación no modifica `domain/` ni `data/`. Si falta algo en
una interfaz de repositorio, se pide, no se cambia por cuenta propia.

## Forma de trabajar

- Explicar el plan antes de cambiar código; cambios pequeños y revisables
- No modificar `build.gradle.kts` ni `libs.versions.toml` sin avisar primero
- `google-services.json` NO está en el repositorio (está en `.gitignore`):
  cada integrante necesita su propia copia para que el proyecto compile
- Compilar después de cada cambio. En Windows/PowerShell: `.\gradlew.bat assembleDebug`
  (en Git Bash o macOS/Linux: `./gradlew assembleDebug`)
- Trabajar siempre en una rama, nunca directo sobre `main`
