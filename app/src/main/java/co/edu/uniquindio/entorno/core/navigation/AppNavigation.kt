package co.edu.uniquindio.entorno.core.navigation

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import co.edu.uniquindio.entorno.features.auth.ForgotPasswordScreen
import co.edu.uniquindio.entorno.features.auth.RegisterScreen
import co.edu.uniquindio.entorno.features.legal.PrivacyScreen
import co.edu.uniquindio.entorno.features.legal.TermsScreen
import co.edu.uniquindio.entorno.features.login.LoginScreen
import co.edu.uniquindio.entorno.features.welcome.WelcomeScreen
import androidx.lifecycle.viewmodel.compose.viewModel
import co.edu.uniquindio.entorno.features.home.HomeScreen
import co.edu.uniquindio.entorno.features.home.SampleReports
import co.edu.uniquindio.entorno.features.moderator.PanelModeradorScreen
import co.edu.uniquindio.entorno.features.moderator.PanelModeradorViewModel
import co.edu.uniquindio.entorno.features.moderator.RevisarPublicacionScreen
import co.edu.uniquindio.entorno.features.moderator.RevisarPublicacionViewModel
import co.edu.uniquindio.entorno.features.notifications.NotificacionesScreen
import co.edu.uniquindio.entorno.features.notifications.NotificacionesViewModel
import co.edu.uniquindio.entorno.features.profile.EditarPerfilScreen
import co.edu.uniquindio.entorno.features.profile.EditarPerfilViewModel
import co.edu.uniquindio.entorno.features.profile.PerfilScreen
import co.edu.uniquindio.entorno.features.profile.PerfilViewModel
import co.edu.uniquindio.entorno.features.report.CommentsScreen
import co.edu.uniquindio.entorno.features.report.MisReportesScreen
import co.edu.uniquindio.entorno.features.report.MisReportesViewModel
import co.edu.uniquindio.entorno.features.report.ReportDetailScreen
import co.edu.uniquindio.entorno.features.report.SampleComments
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll

object Routes {
    const val WELCOME = "welcome"
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val FORGOT_PASSWORD = "forgot_password"
    const val TERMS = "terms"
    const val PRIVACY = "privacy"
    const val MAIN = "main"
    
    // Nuevas pantallas
    const val HOME = "home"
    const val PROFILE = "profile"
    const val EDIT_PROFILE = "edit_profile"
    const val MY_REPORTS = "my_reports"
    const val REPORT_DETAIL = "report_detail"
    const val COMMENTS = "comments"
    const val MODERATOR_PANEL = "moderator_panel"
    const val REVIEW_REPORT = "review_report"
    const val NOTIFICATIONS = "notifications"
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Routes.WELCOME
    ) {
        composable(Routes.WELCOME) {
            WelcomeScreen(
                onNavigateToRegister = { navController.navigate(Routes.REGISTER) },
                onNavigateToLogin = { navController.navigate(Routes.LOGIN) },
                onNavigateToTerms = { navController.navigate(Routes.TERMS) },
                onNavigateToPrivacy = { navController.navigate(Routes.PRIVACY) }
            )
        }

        composable(Routes.LOGIN) {
            LoginScreen(
                onNavigateToRegister = { navController.navigate(Routes.REGISTER) },
                onNavigateToForgotPassword = { navController.navigate(Routes.FORGOT_PASSWORD) },
                onLoginSuccess = {
                    navController.navigate(Routes.MAIN) {
                        popUpTo(Routes.WELCOME) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.REGISTER) {
            RegisterScreen(
                onNavigateToLogin = { navController.navigate(Routes.LOGIN) },
                onRegisterSuccess = {
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(Routes.REGISTER) { inclusive = true }
                    }
                },
                onNavigateToTerms = { navController.navigate(Routes.TERMS) },
                onNavigateToPrivacy = { navController.navigate(Routes.PRIVACY) }
            )
        }

        composable(Routes.FORGOT_PASSWORD) {
            ForgotPasswordScreen(
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(Routes.TERMS) {
            TermsScreen(
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(Routes.PRIVACY) {
            PrivacyScreen(
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(Routes.MAIN) {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = Color.White
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp)
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "¡Bienvenido a Entorno!",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF111318)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Has iniciado sesión exitosamente.",
                        fontSize = 16.sp,
                        color = Color(0xFF5A5F73)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Botones para acceder a las nuevas pantallas
                    Button(onClick = { navController.navigate(Routes.HOME) }, modifier = Modifier.fillMaxWidth()) { Text("Home") }
                    Button(onClick = { navController.navigate(Routes.PROFILE) }, modifier = Modifier.fillMaxWidth()) { Text("Perfil") }
                    Button(onClick = { navController.navigate(Routes.MY_REPORTS) }, modifier = Modifier.fillMaxWidth()) { Text("Mis Reportes") }
                    Button(onClick = { navController.navigate(Routes.MODERATOR_PANEL) }, modifier = Modifier.fillMaxWidth()) { Text("Panel Moderador") }
                    Button(onClick = { navController.navigate(Routes.NOTIFICATIONS) }, modifier = Modifier.fillMaxWidth()) { Text("Notificaciones") }
                    
                    Spacer(modifier = Modifier.height(32.dp))
                    Button(
                        onClick = {
                            navController.navigate(Routes.WELCOME) {
                                popUpTo(Routes.MAIN) { inclusive = true }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF303CA2))
                    ) {
                        Text(text = "Cerrar sesión / Volver", color = Color.White)
                    }
                }
            }
        }

        composable(Routes.HOME) {
            HomeScreen(
                userName = "Juan Cayón",
                reports = SampleReports.reports,
                onNotificationsClick = { navController.navigate(Routes.NOTIFICATIONS) },
                onReportClick = { _ -> navController.navigate(Routes.REPORT_DETAIL) }
            )
        }

        composable(Routes.PROFILE) {
            val viewModel: PerfilViewModel = viewModel()
            PerfilScreen(
                viewModel = viewModel,
                onEditarClick = { navController.navigate(Routes.EDIT_PROFILE) }
            )
        }

        composable(Routes.EDIT_PROFILE) {
            val viewModel: EditarPerfilViewModel = viewModel()
            EditarPerfilScreen(
                viewModel = viewModel,
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(Routes.MY_REPORTS) {
            val viewModel: MisReportesViewModel = viewModel()
            MisReportesScreen(
                viewModel = viewModel,
                onReporteClick = { _ -> navController.navigate(Routes.REPORT_DETAIL) }
            )
        }

        composable(Routes.REPORT_DETAIL) {
            ReportDetailScreen(
                report = SampleReports.reports.first(),
                onBack = { navController.popBackStack() },
                onCommentsClick = { navController.navigate(Routes.COMMENTS) }
            )
        }

        composable(Routes.COMMENTS) {
            CommentsScreen(
                report = SampleReports.reports.first(),
                comments = SampleComments.comments,
                currentUserId = SampleComments.CURRENT_USER_ID,
                moderatorIds = SampleComments.moderatorIds,
                onBack = { navController.popBackStack() }
            )
        }

        composable(Routes.MODERATOR_PANEL) {
            val viewModel: PanelModeradorViewModel = viewModel()
            PanelModeradorScreen(
                viewModel = viewModel,
                onReporteClick = { _ -> navController.navigate(Routes.REVIEW_REPORT) }
            )
        }

        composable(Routes.REVIEW_REPORT) {
            val viewModel: RevisarPublicacionViewModel = viewModel()
            RevisarPublicacionScreen(
                viewModel = viewModel,
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(Routes.NOTIFICATIONS) {
            val viewModel: NotificacionesViewModel = viewModel()
            NotificacionesScreen(
                viewModel = viewModel,
                onBackClick = { navController.popBackStack() }
            )
        }
    }
}
