package co.edu.uniquindio.entorno.features.login

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import co.edu.uniquindio.entorno.core.component.AppTextField
import co.edu.uniquindio.entorno.core.component.AuthHeader
import co.edu.uniquindio.entorno.core.component.AuthTab
import co.edu.uniquindio.entorno.core.component.AuthTabSelector
import co.edu.uniquindio.entorno.core.util.RequestResult

@Composable
fun LoginScreen(
    onNavigateToRegister: () -> Unit,
    onNavigateToForgotPassword: () -> Unit,
    onLoginSuccess: () -> Unit,
    viewModel: LoginViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var passwordVisible by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.loginResult) {
        when (val result = uiState.loginResult) {
            is RequestResult.Success -> onLoginSuccess()
            is RequestResult.Failure -> {
                snackbarHostState.showSnackbar(result.errorMessage)
                viewModel.clearResult()
            }
            else -> Unit
        }
    }

    Scaffold(
        containerColor = Color.White,
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(24.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            AuthHeader()

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Bienvenido de vuelta",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF111318)
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Entra para ver y crear reportes en tu zona.",
                fontSize = 15.sp,
                color = Color(0xFF5A5F73)
            )

            Spacer(modifier = Modifier.height(24.dp))

            AuthTabSelector(
                selectedTab = AuthTab.LOGIN,
                onTabSelected = { tab ->
                    if (tab == AuthTab.REGISTER) {
                        onNavigateToRegister()
                    }
                }
            )

            Spacer(modifier = Modifier.height(28.dp))

            AppTextField(
                value = uiState.email,
                onValueChange = { viewModel.onEmailChange(it) },
                label = "Correo electrónico",
                placeholder = "ejemplo@uqvirtual.edu.co",
                leadingIcon = Icons.Default.Email,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                errorMessage = uiState.emailError
            )

            Spacer(modifier = Modifier.height(16.dp))

            AppTextField(
                value = uiState.password,
                onValueChange = { viewModel.onPasswordChange(it) },
                label = "Contraseña",
                placeholder = "••••••••",
                leadingIcon = Icons.Default.Lock,
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                errorMessage = uiState.passwordError,
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = if (passwordVisible) "Ocultar contraseña" else "Mostrar contraseña",
                            tint = Color(0xFF74798D)
                        )
                    }
                }
            )

            Spacer(modifier = Modifier.height(8.dp))

            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.CenterEnd
            ) {
                Text(
                    text = "¿Olvidaste tu contraseña?",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF303CA2),
                    modifier = Modifier
                        .clickable { onNavigateToForgotPassword() }
                        .padding(vertical = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = { viewModel.login() },
                enabled = uiState.isFormValid && uiState.loginResult !is RequestResult.Loading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF303CA2),
                    disabledContainerColor = Color(0xFFD1D5DB)
                )
            ) {
                if (uiState.loginResult is RequestResult.Loading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Text(
                        text = "Iniciar sesión",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true, widthDp = 392, heightDp = 800)
@Composable
fun LoginScreenPreview() {
    LoginScreen(
        onNavigateToRegister = {},
        onNavigateToForgotPassword = {},
        onLoginSuccess = {}
    )
}
