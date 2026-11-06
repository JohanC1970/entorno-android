package co.edu.uniquindio.entorno.core.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
    val state by sessionViewModel.state.collectAsStateWithLifecycle()

    when (val current = state) {
        SessionState.Loading -> Box(
            modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
            contentAlignment = Alignment.Center
        ) { CircularProgressIndicator() }
        SessionState.SignedOut -> AuthNavigation()
        is SessionState.Active -> MainScreen(session = current.session)
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
