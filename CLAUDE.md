# Entorno — app Android de reportes ciudadanos

Proyecto final de Construcción de Aplicaciones Móviles (Universidad del Quindío).
Kotlin + Jetpack Compose + Material 3. Paquete: `co.edu.uniquindio.entorno`
Equipo: Juan Esteban Cayón, Johan Camilo García, Juan Andrés Pérez.

## Fase actual

Fase 2: pantallas, navegación y lógica con datos EN MEMORIA.
No agregar Firebase, Supabase, Room, Retrofit ni ninguna persistencia todavía.

## Estructura de paquetes

La del repo guía del curso (guía 06, "Arquitectura del proyecto"):

```
co.edu.uniquindio.entorno/
├── core/
│   ├── component/      composables reutilizables (ReporteCard, CategoriaChip, EstadoPill)
│   ├── navigation/     NavHost y rutas
│   ├── theme/          Color.kt, Theme.kt, Type.kt
│   └── util/           RequestResult.kt y helpers
├── data/
│   ├── model/          Reporte, Usuario, Comentario
│   └── repository/     repositorios en memoria
├── features/
│   ├── login/          LoginScreen.kt + LoginViewModel.kt
│   ├── home/
│   ├── report/
│   ├── moderator/
│   └── profile/
└── MainActivity.kt
```

No crear la capa `domain/` ni casos de uso por ahora: el repo guía del profesor
tampoco la tiene y en Fase 2 solo agregaría ruido. Si aparece lógica de negocio
que se repita entre pantallas, se evalúa entonces.

## Arquitectura

- Cada pantalla: `XxxScreen.kt` + `XxxViewModel.kt` en `features/<feature>/`
- Estado: `data class XxxUiState` inmutable; `MutableStateFlow` privado expuesto
  con `asStateFlow()`; cambios con `_uiState.update { it.copy(...) }`
- Resultado de operaciones: `sealed class RequestResult { Loading, Success, Failure }`
  en `core/util/`
- Asincronía: `viewModelScope.launch` + `delay()` para simular el backend
- Sin inyección de dependencias (nada de Hilt/Koin): ViewModels con `viewModel()`
- Flujo de datos unidireccional: el estado baja como parámetro, los eventos suben
  como lambdas (`onClick`, `onTitleChange`)

## Reglas de Compose

- Los composables de `core/component/` son **sin estado**: reciben datos y lambdas,
  nunca leen el ViewModel por dentro
- Colores y tipografías **siempre** desde `MaterialTheme.colorScheme` y
  `MaterialTheme.typography`. Nunca `Color(0xFF...)` dentro de una pantalla
- `dynamicColor = false` en `EntornoTheme`: con `true`, Android 12+ reemplaza
  nuestra paleta por los colores del fondo de pantalla del usuario
- Cada `XxxScreen` lleva su `@Preview` con `showBackground = true`
- Toda pantalla que pueda crecer usa `LazyColumn`, no `Column` + `verticalScroll`

## Reglas de Kotlin (criterios de calificación del curso)

- `val` sobre `var`; nunca usar `!!`
- Estados, categorías, roles y niveles como `enum class` o `sealed class`, nunca `String`
- `if` / `when` / `try` como expresiones en vez de asignaciones intermedias
- Nombres del dominio en español, como ya están en el repo

## Dominio

- Categorías: SEGURIDAD, EMERGENCIAS_MEDICAS, INFRAESTRUCTURA, MASCOTAS, COMUNIDAD
- Estados: PENDIENTE, EN_VERIFICACION, VERIFICADO, RECHAZADO, RESUELTO
  (PENDIENTE y EN_VERIFICACION son el mismo estado: al ciudadano se le muestra
  "En verificación", al moderador "Pendiente")
- Roles: USUARIO, MODERADOR
- Niveles: NOVATO, COLABORADOR, GUARDIAN, HEROE_COMUNITARIO
- Severidad: ALTA, MEDIA, BAJA, calculada a partir de los votos "Es importante"
- `Comentario` admite respuestas anidadas: `comentarioPadreId: String?`

## Pantallas (14 mockups aprobados en Fase 1)

Splash, Login/Registro, Home Feed, Mapa de reportes, Detalle del reporte,
Comentarios, Crear reporte, Mis reportes, Notificaciones, Perfil,
Héroes comunitarios, Estadísticas, Configuración, y **Mapa de calor**
(requisito adicional del proyecto; usa una escala secuencial de un solo tono
y un filtro por franja horaria).

## Diseño

- Primario `#2F3BA2` · onPrimary `#FFFFFF` · primaryContainer `#E2E4FF`
- Fondo `#FAFAFC` · tarjetas `#FFFFFF` · superficie alterna `#F1F2F7`
- Texto `#1A1B25` / `#5A5C6E` · divisores `#DFE1EA`
- Titulares: Archivo · Interfaz: Public Sans
- Las 5 categorías nunca se distinguen solo por color: siempre icono + etiqueta

## Forma de trabajar

- Explicar el plan antes de cambiar código; cambios pequeños y revisables
- No modificar `build.gradle.kts` ni `libs.versions.toml` sin avisar primero:
  son archivos compartidos por los tres y generan conflictos en Git
- Compilar después de cada cambio. En Windows/PowerShell: `.\gradlew.bat assembleDebug`
  (en Git Bash o macOS/Linux: `./gradlew assembleDebug`)
- Trabajar siempre en una rama, nunca directo sobre `main`
