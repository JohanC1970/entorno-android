package co.edu.uniquindio.entorno.core.navigation

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Person
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
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import co.edu.uniquindio.entorno.domain.model.Location
import co.edu.uniquindio.entorno.domain.model.Report
import co.edu.uniquindio.entorno.domain.model.UserRole
import co.edu.uniquindio.entorno.domain.model.UserSession
import co.edu.uniquindio.entorno.features.auth.ChangePasswordScreen
import co.edu.uniquindio.entorno.features.community.HeroesScreen
import co.edu.uniquindio.entorno.features.home.HomeScreen
import co.edu.uniquindio.entorno.features.legal.PrivacyScreen
import co.edu.uniquindio.entorno.features.legal.TermsScreen
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
import kotlin.reflect.KClass

/** Destino de la barra inferior: [route] es a donde se navega y [routeClass] sirve para saber si está activo. */
private data class BottomDestination(
    val label: String,
    val route: Any,
    val routeClass: KClass<*>,
    val icon: ImageVector
)

private val citizenDestinations = listOf(
    BottomDestination("Inicio", Home, Home::class, Icons.Filled.Home),
    BottomDestination("Mapa", MapRoute(), MapRoute::class, Icons.Filled.Map),
    BottomDestination("Mis reportes", MyReports, MyReports::class, Icons.Filled.Description),
    BottomDestination("Perfil", Profile, Profile::class, Icons.Filled.Person)
)

private val moderatorDestination =
    BottomDestination("Moderación", ModeratorPanel, ModeratorPanel::class, Icons.Filled.AdminPanelSettings)

/**
 * App principal: barra inferior (Inicio, Mapa, Mis reportes, Perfil y, para moderadores,
 * Moderación) y el grafo con el resto de pantallas.
 */
@Composable
fun MainScreen(session: UserSession) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = backStackEntry?.destination

    val destinations = if (session.role == UserRole.MODERATOR) {
        citizenDestinations + moderatorDestination
    } else {
        citizenDestinations
    }
    val showBottomBar = destinations.any { currentDestination?.hasRoute(it.routeClass) == true }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        // Cada pantalla maneja sus propios insets; solo se reserva el espacio de la barra inferior.
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    destinations.forEach { destination ->
                        NavigationBarItem(
                            selected = currentDestination?.hierarchy
                                ?.any { it.hasRoute(destination.routeClass) } == true,
                            onClick = {
                                navController.navigate(destination.route) {
                                    popUpTo<Home> { saveState = true }
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

    NavHost(navController = navController, startDestination = Home, modifier = modifier) {

        // ---------------------------------------------------------- pestañas
        composable<Home> {
            HomeScreen(
                onNotificationsClick = { navController.navigate(Notifications) },
                onReportClick = { report -> navController.navigate(ReportDetail(report.id)) },
                onCreateClick = { navController.navigate(CreateReport()) },
                onMapClick = { navController.navigate(MapRoute()) }
            )
        }

        composable<MapRoute> {
            MapScreen(
                onReportClick = { id -> navController.navigate(ReportDetail(id)) }
            )
        }

        composable<MyReports> {
            MisReportesScreen(
                onReporteClick = { id -> navController.navigate(ReportDetail(id)) }
            )
        }

        composable<Profile> {
            PerfilScreen(
                onEditarClick = { navController.navigate(EditProfile) },
                onSettingsClick = { navController.navigate(Settings) },
                onBadgesClick = { navController.navigate(Badges) },
                onHeroesClick = { navController.navigate(Heroes) },
                onStatsClick = { navController.navigate(Stats) },
                onModeratorClick = { navController.navigate(ModeratorPanel) }
            )
        }

        composable<ModeratorPanel> {
            PanelModeradorScreen(
                onReporteClick = { id -> navController.navigate(ReviewReport(id)) }
            )
        }

        // ---------------------------------------------------------- reportes
        composable<ReportDetail> {
            ReportDetailScreen(
                onBack = { navController.popBackStack() },
                onShare = { report -> shareReport(context, report) },
                onCommentsClick = { id -> navController.navigate(Comments(id)) },
                onViewOnMap = { id -> navController.navigate(MapRoute(focus = id)) }
            )
        }

        composable<Comments> {
            CommentsRoute(onBack = { navController.popBackStack() })
        }

        composable<CreateReport> {
            CreateReportScreen(
                onClose = { navController.popBackStack() },
                onPublished = { navController.popBackStack() },
                onChangeLocation = { navController.navigate(PickLocation) }
            )
        }

        composable<PickLocation> {
            // Punto ya elegido antes; null la primera vez, para que el selector use el GPS.
            val initial = navController.previousBackStackEntry
                ?.savedStateHandle?.get<String>(KEY_PICKED_LOCATION)
                ?.split(",")
                ?.let { parts ->
                    val lat = parts.getOrNull(0)?.toDoubleOrNull()
                    val lon = parts.getOrNull(1)?.toDoubleOrNull()
                    if (lat != null && lon != null) Location(lat, lon) else null
                }
            LocationPickerScreen(
                initial = initial,
                onBack = { navController.popBackStack() },
                onConfirm = { place ->
                    navController.previousBackStackEntry
                        ?.savedStateHandle
                        ?.set(KEY_PICKED_LOCATION, "${place.location.latitude},${place.location.longitude}")
                    navController.popBackStack()
                }
            )
        }

        composable<ReviewReport> {
            RevisarPublicacionScreen(onBackClick = { navController.popBackStack() })
        }

        // ---------------------------------------------------------- cuenta y comunidad
        composable<Notifications> {
            NotificacionesScreen(
                onBackClick = { navController.popBackStack() },
                onNotificationClick = { reportId -> navController.navigate(ReportDetail(reportId)) }
            )
        }

        composable<EditProfile> {
            EditarPerfilScreen(onBackClick = { navController.popBackStack() })
        }

        composable<Settings> {
            SettingsScreen(
                onBack = { navController.popBackStack() },
                onEditProfile = { navController.navigate(EditProfile) },
                onChangePassword = { navController.navigate(ChangePassword) },
                onTerms = { navController.navigate(Terms) },
                onPrivacy = { navController.navigate(Privacy) }
            )
        }

        composable<ChangePassword> {
            ChangePasswordScreen(onBack = { navController.popBackStack() })
        }

        composable<Heroes> { HeroesScreen(onBack = { navController.popBackStack() }) }
        composable<Stats> { StatsScreen(onBack = { navController.popBackStack() }) }
        composable<Badges> { InsigniasScreen(onBack = { navController.popBackStack() }) }

        composable<Terms> { TermsScreen(onBackClick = { navController.popBackStack() }) }
        composable<Privacy> { PrivacyScreen(onBackClick = { navController.popBackStack() }) }
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