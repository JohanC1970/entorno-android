package co.edu.uniquindio.entorno.core.navigation

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Description
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import co.edu.uniquindio.entorno.domain.model.Location
import co.edu.uniquindio.entorno.domain.model.Report
import co.edu.uniquindio.entorno.domain.model.UserRole
import co.edu.uniquindio.entorno.domain.model.UserSession
import co.edu.uniquindio.entorno.features.auth.ChangePasswordScreen
import co.edu.uniquindio.entorno.features.community.HeroesScreen
import co.edu.uniquindio.entorno.features.home.HomeScreen
import co.edu.uniquindio.entorno.features.legal.PrivacyScreen
import co.edu.uniquindio.entorno.features.legal.TermsScreen
import co.edu.uniquindio.entorno.features.map.HeatMapScreen
import co.edu.uniquindio.entorno.features.map.LocationPickerScreen
import co.edu.uniquindio.entorno.features.map.MapScreen
import co.edu.uniquindio.entorno.features.moderator.PanelModeradorScreen
import co.edu.uniquindio.entorno.features.moderator.RevisarPublicacionScreen
import co.edu.uniquindio.entorno.features.notifications.NotificacionesScreen
import co.edu.uniquindio.entorno.features.profile.EditarPerfilScreen
import co.edu.uniquindio.entorno.features.profile.InsigniasScreen
import co.edu.uniquindio.entorno.features.profile.PerfilScreen
import co.edu.uniquindio.entorno.features.report.CommentsRoute
import co.edu.uniquindio.entorno.features.report.CreateReportScreen
import co.edu.uniquindio.entorno.features.report.KEY_PICKED_LOCATION
import co.edu.uniquindio.entorno.features.report.MisReportesScreen
import co.edu.uniquindio.entorno.features.report.ReportDetailScreen
import co.edu.uniquindio.entorno.features.settings.SettingsScreen
import co.edu.uniquindio.entorno.features.stats.StatsScreen

/** Destino de la barra inferior. [pattern] es la ruta registrada en el NavHost. */
private data class BottomDestination(
    val label: String,
    val navigateRoute: String,
    val pattern: String,
    val icon: ImageVector
)

private val citizenDestinations = listOf(
    BottomDestination("Inicio", Routes.HOME, Routes.HOME, Icons.Filled.Home),
    BottomDestination("Mapa", Routes.MAP_BASE, Routes.MAP, Icons.Filled.Map),
    BottomDestination("Mis reportes", Routes.MY_REPORTS, Routes.MY_REPORTS, Icons.Filled.Description),
    BottomDestination("Perfil", Routes.PROFILE, Routes.PROFILE, Icons.Filled.Person)
)

private val moderatorDestination =
    BottomDestination("Moderación", Routes.MODERATOR_PANEL, Routes.MODERATOR_PANEL, Icons.Filled.AdminPanelSettings)

/**
 * App principal: barra inferior (Inicio, Mapa, Mis reportes, Perfil y, para moderadores,
 * Moderación) y el grafo con el resto de pantallas.
 */
@Composable
fun MainScreen(session: UserSession) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    val destinations = if (session.role == UserRole.MODERATOR) {
        citizenDestinations + moderatorDestination
    } else {
        citizenDestinations
    }
    val showBottomBar = destinations.any { it.pattern == currentRoute }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        // Cada pantalla maneja sus propios insets; solo se reserva el espacio de la barra inferior.
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    destinations.forEach { destination ->
                        NavigationBarItem(
                            selected = destination.pattern == currentRoute,
                            onClick = {
                                navController.navigate(destination.navigateRoute) {
                                    popUpTo(Routes.HOME) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(destination.icon, contentDescription = destination.label) },
                            label = { Text(destination.label) }
                        )
                    }
                }
            }
        }
    ) { padding ->
        MainNavHost(
            navController = navController,
            modifier = Modifier.padding(padding).consumeWindowInsets(padding)
        )
    }
}

@Composable
private fun MainNavHost(navController: NavHostController, modifier: Modifier) {
    val context = LocalContext.current

    NavHost(navController = navController, startDestination = Routes.HOME, modifier = modifier) {

        // ---------------------------------------------------------- pestañas
        composable(Routes.HOME) {
            HomeScreen(
                onNotificationsClick = { navController.navigate(Routes.NOTIFICATIONS) },
                onReportClick = { report -> navController.navigate(Routes.reportDetail(report.id)) },
                onCreateClick = { navController.navigate(Routes.CREATE_REPORT) },
                onMapClick = { navController.navigate(Routes.map()) }
            )
        }

        composable(
            route = Routes.MAP,
            arguments = listOf(
                navArgument("focus") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) {
            MapScreen(
                onReportClick = { id -> navController.navigate(Routes.reportDetail(id)) },
                onHeatMapClick = { navController.navigate(Routes.HEAT_MAP) }
            )
        }

        composable(Routes.MY_REPORTS) {
            MisReportesScreen(
                onReporteClick = { id -> navController.navigate(Routes.reportDetail(id)) }
            )
        }

        composable(Routes.PROFILE) {
            PerfilScreen(
                onEditarClick = { navController.navigate(Routes.EDIT_PROFILE) },
                onSettingsClick = { navController.navigate(Routes.SETTINGS) },
                onBadgesClick = { navController.navigate(Routes.BADGES) },
                onHeroesClick = { navController.navigate(Routes.HEROES) },
                onStatsClick = { navController.navigate(Routes.STATS) },
                onModeratorClick = { navController.navigate(Routes.MODERATOR_PANEL) }
            )
        }

        composable(Routes.MODERATOR_PANEL) {
            PanelModeradorScreen(
                onReporteClick = { id -> navController.navigate(Routes.reviewReport(id)) }
            )
        }

        // ---------------------------------------------------------- reportes
        composable(Routes.REPORT_DETAIL) {
            ReportDetailScreen(
                onBack = { navController.popBackStack() },
                onShare = { report -> shareReport(context, report) },
                onCommentsClick = { id -> navController.navigate(Routes.comments(id)) },
                onViewOnMap = { id -> navController.navigate(Routes.map(id)) }
            )
        }

        composable(Routes.COMMENTS) {
            CommentsRoute(onBack = { navController.popBackStack() })
        }

        composable(Routes.CREATE_REPORT) {
            CreateReportScreen(
                onClose = { navController.popBackStack() },
                onPublished = { navController.popBackStack() },
                onChangeLocation = { navController.navigate(Routes.PICK_LOCATION) }
            )
        }

        composable(Routes.PICK_LOCATION) {
            // El punto inicial es el que ya tenía el reporte (ubicación por defecto si no hay).
            val initial = navController.previousBackStackEntry
                ?.savedStateHandle?.get<String>(KEY_PICKED_LOCATION)
                ?.split(",")
                ?.let { parts ->
                    val lat = parts.getOrNull(0)?.toDoubleOrNull()
                    val lon = parts.getOrNull(1)?.toDoubleOrNull()
                    if (lat != null && lon != null) Location(lat, lon) else null
                } ?: co.edu.uniquindio.entorno.core.util.Geo.ARMENIA_CENTER
            LocationPickerScreen(
                initial = initial,
                onBack = { navController.popBackStack() },
                onConfirm = { picked ->
                    navController.previousBackStackEntry
                        ?.savedStateHandle
                        ?.set(KEY_PICKED_LOCATION, "${picked.latitude},${picked.longitude}")
                    navController.popBackStack()
                }
            )
        }

        composable(Routes.REVIEW_REPORT) {
            RevisarPublicacionScreen(onBackClick = { navController.popBackStack() })
        }

        // ---------------------------------------------------------- cuenta y comunidad
        composable(Routes.NOTIFICATIONS) {
            NotificacionesScreen(
                onBackClick = { navController.popBackStack() },
                onNotificationClick = { reportId -> navController.navigate(Routes.reportDetail(reportId)) }
            )
        }

        composable(Routes.EDIT_PROFILE) {
            EditarPerfilScreen(onBackClick = { navController.popBackStack() })
        }

        composable(Routes.SETTINGS) {
            SettingsScreen(
                onBack = { navController.popBackStack() },
                onEditProfile = { navController.navigate(Routes.EDIT_PROFILE) },
                onChangePassword = { navController.navigate(Routes.CHANGE_PASSWORD) },
                onTerms = { navController.navigate(Routes.TERMS) },
                onPrivacy = { navController.navigate(Routes.PRIVACY) }
            )
        }

        composable(Routes.CHANGE_PASSWORD) {
            ChangePasswordScreen(onBack = { navController.popBackStack() })
        }

        composable(Routes.HEROES) { HeroesScreen(onBack = { navController.popBackStack() }) }
        composable(Routes.STATS) { StatsScreen(onBack = { navController.popBackStack() }) }
        composable(Routes.BADGES) { InsigniasScreen(onBack = { navController.popBackStack() }) }
        composable(Routes.HEAT_MAP) { HeatMapScreen(onBack = { navController.popBackStack() }) }

        composable(Routes.TERMS) { TermsScreen(onBackClick = { navController.popBackStack() }) }
        composable(Routes.PRIVACY) { PrivacyScreen(onBackClick = { navController.popBackStack() }) }
    }
}

/** Comparte el reporte con otras apps (WhatsApp, correo, etc.). */
private fun shareReport(context: Context, report: Report) {
    val text = buildString {
        append("Reporte en Entorno: ").append(report.title).append('\n')
        append(report.description).append('\n')
        append("Zona: ").append(report.neighborhood.ifBlank { "Armenia" })
    }
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, text)
    }
    context.startActivity(Intent.createChooser(intent, "Compartir reporte"))
}
