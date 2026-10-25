package co.edu.uniquindio.entorno.features.auth

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import co.edu.uniquindio.entorno.core.component.AppTextField
import co.edu.uniquindio.entorno.core.component.AuthHeader
import co.edu.uniquindio.entorno.core.util.RequestResult

@Composable
fun ForgotPasswordScreen(
    onBackClick: () -> Unit,
    viewModel: ForgotPasswordViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.result) {
        when (val result = uiState.result) {
            is RequestResult.Success -> {
                snackbarHostState.showSnackbar(result.message)
                viewModel.clearResult()
            }
            is RequestResult.Failure -> {
                snackbarHostState.showSnackbar(result.errorMessage)
                viewModel.clearResult()
            }
            else -> Unit
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
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
            // Botón de retroceso
            IconButton(onClick = onBackClick, modifier = Modifier.padding(start = 0.dp)) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Volver",
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            AuthHeader()

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Recuperar contraseña",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Ingresa tu correo electrónico y te enviaremos un enlace para restablecer tu contraseña.",
                fontSize = 15.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(32.dp))

            AppTextField(
                value = uiState.email,
                onValueChange = { viewModel.onEmailChange(it) },
                label = "Correo electrónico",
                placeholder = "ejemplo@uqvirtual.edu.co",
                leadingIcon = Icons.Default.Email,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                errorMessage = uiState.emailError
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = { viewModel.sendResetLink() },
                enabled = uiState.isFormValid && uiState.result !is RequestResult.Loading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                if (uiState.result is RequestResult.Loading) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(24.dp))
                } else {
                    Text(
                        text = "Enviar enlace",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}
