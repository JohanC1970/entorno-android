package co.edu.uniquindio.entorno.core.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import co.edu.uniquindio.entorno.features.auth.ForgotPasswordScreen
import co.edu.uniquindio.entorno.features.auth.RegisterScreen
import co.edu.uniquindio.entorno.features.legal.PrivacyScreen
import co.edu.uniquindio.entorno.features.legal.TermsScreen
import co.edu.uniquindio.entorno.features.login.LoginScreen
import co.edu.uniquindio.entorno.features.welcome.WelcomeScreen

/**
 * Raíz de la navegación. Se decide por la sesión: sin sesión se muestra el flujo de
 * autenticación; con sesión, la app principal (con barra inferior según el rol).
 * Al iniciar o cerrar sesión (o eliminar la cuenta) el cambio ocurre solo.
 */
@Composable
fun AppNavigation(sessionViewModel: SessionViewModel = hiltViewModel()) {
    val session by sessionViewModel.session.collectAsStateWithLifecycle()
    val current = session

    if (current == null) {
        AuthNavigation()
    } else {
        MainScreen(session = current)
    }
}

@Composable
private fun AuthNavigation() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = Welcome) {
        composable<Welcome> {
            WelcomeScreen(
                onNavigateToRegister = { navController.navigate(Register) },
                onNavigateToLogin = { navController.navigate(Login) },
                onNavigateToTerms = { navController.navigate(Terms) },
                onNavigateToPrivacy = { navController.navigate(Privacy) }
            )
        }

        composable<Login> {
            LoginScreen(
                onNavigateToRegister = { navController.navigate(Register) },
                onNavigateToForgotPassword = { navController.navigate(ForgotPassword) },
                // Al guardarse la sesión, AppNavigation cambia sola a la app principal.
                onLoginSuccess = { }
            )
        }

        composable<Register> {
            RegisterScreen(
                onNavigateToLogin = { navController.navigate(Login) },
                onRegisterSuccess = {
                    navController.navigate(Login) {
                        popUpTo<Register> { inclusive = true }
                    }
                },
                onNavigateToTerms = { navController.navigate(Terms) },
                onNavigateToPrivacy = { navController.navigate(Privacy) }
            )
        }

        composable<ForgotPassword> {
            ForgotPasswordScreen(onBackClick = { navController.popBackStack() })
        }

        composable<Terms> {
            TermsScreen(onBackClick = { navController.popBackStack() })
        }

        composable<Privacy> {
            PrivacyScreen(onBackClick = { navController.popBackStack() })
        }
    }
}
