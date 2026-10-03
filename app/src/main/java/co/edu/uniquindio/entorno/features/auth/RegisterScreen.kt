package co.edu.uniquindio.entorno.features.auth

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.LocationCity
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import co.edu.uniquindio.entorno.core.component.AppTextField
import co.edu.uniquindio.entorno.core.component.AuthHeader
import co.edu.uniquindio.entorno.core.component.AuthTab
import co.edu.uniquindio.entorno.core.component.AuthTabSelector
import co.edu.uniquindio.entorno.core.util.RequestResult

@Composable
fun RegisterScreen(
    onNavigateToLogin: () -> Unit,
    onRegisterSuccess: () -> Unit,
    viewModel: RegisterViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.registerResult) {
        if (uiState.registerResult is RequestResult.Success) {
            onRegisterSuccess()
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color.White
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            AuthHeader()

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Crea tu cuenta",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF111318)
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Únete para reportar y seguir lo que pasa en tu zona.",
                fontSize = 15.sp,
                color = Color(0xFF5A5F73)
            )

            Spacer(modifier = Modifier.height(24.dp))

            AuthTabSelector(
                selectedTab = AuthTab.REGISTER,
                onTabSelected = { tab ->
                    if (tab == AuthTab.LOGIN) {
                        onNavigateToLogin()
                    }
                }
            )

            Spacer(modifier = Modifier.height(28.dp))

            // Nombre completo
            AppTextField(
                value = uiState.name,
                onValueChange = { viewModel.onNameChange(it) },
                label = "Nombre completo",
                placeholder = "Johan García",
                leadingIcon = Icons.Default.Person,
                errorMessage = uiState.nameError
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Correo electrónico
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

            // Ciudad
            AppTextField(
                value = uiState.city,
                onValueChange = { viewModel.onCityChange(it) },
                label = "Ciudad",
                placeholder = "Armenia",
                leadingIcon = Icons.Default.LocationCity,
                errorMessage = uiState.cityError
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Dirección
            AppTextField(
                value = uiState.address,
                onValueChange = { viewModel.onAddressChange(it) },
                label = "Dirección",
                placeholder = "Calle 12 # 23-45",
                leadingIcon = Icons.Default.Place,
                errorMessage = uiState.addressError
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Contraseña
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

            Spacer(modifier = Modifier.height(16.dp))

            // Confirmar contraseña
            AppTextField(
                value = uiState.confirmPassword,
                onValueChange = { viewModel.onConfirmPasswordChange(it) },
                label = "Confirmar contraseña",
                placeholder = "••••••••",
                leadingIcon = Icons.Default.Lock,
                visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                errorMessage = uiState.confirmPasswordError,
                trailingIcon = {
                    IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                        Icon(
                            imageVector = if (confirmPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = if (confirmPasswordVisible) "Ocultar contraseña" else "Mostrar contraseña",
                            tint = Color(0xFF74798D)
                        )
                    }
                }
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Texto legal
            val legalText = buildAnnotatedString {
                append("Al registrarte aceptas los ")
                withStyle(
                    style = SpanStyle(
                        color = Color(0xFF303CA2),
                        fontWeight = FontWeight.Medium,
                        textDecoration = TextDecoration.Underline
                    )
                ) {
                    append("Términos")
                }
                append(" y la ")
                withStyle(
                    style = SpanStyle(
                        color = Color(0xFF303CA2),
                        fontWeight = FontWeight.Medium,
                        textDecoration = TextDecoration.Underline
                    )
                ) {
                    append("Política de privacidad")
                }
                append(".")
            }

            Text(
                text = legalText,
                fontSize = 13.sp,
                color = Color(0xFF5A5F73)
            )

            Spacer(modifier = Modifier.height(28.dp))

            Button(
                onClick = {
                    // Nota: para pruebas de UI si location es null, podemos setear una ubicación por defecto o requerirla
                    if (uiState.location == null) {
                        viewModel.onLocationSelected(4.53389, -75.68111) // Armenia por defecto si no seleccionó mapa aún
                    }
                    viewModel.register()
                },
                enabled = uiState.registerResult !is RequestResult.Loading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF303CA2),
                    disabledContainerColor = Color(0xFFD1D5DB)
                )
            ) {
                if (uiState.registerResult is RequestResult.Loading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Text(
                        text = "Crear cuenta",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                }
            }

            // Mensaje de éxito o error en registro
            when (val res = uiState.registerResult) {
                is RequestResult.Success -> {
                    Spacer(modifier = Modifier.height(16.dp))
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFE6F4EA)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = res.message,
                            fontSize = 14.sp,
                            color = Color(0xFF137333),
                            modifier = Modifier.padding(16.dp),
                            textAlign = TextAlign.Center
                        )
                    }
                }
                is RequestResult.Failure -> {
                    Spacer(modifier = Modifier.height(16.dp))
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFCE8E6)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = res.errorMessage,
                            fontSize = 14.sp,
                            color = Color(0xFFC5221F),
                            modifier = Modifier.padding(16.dp),
                            textAlign = TextAlign.Center
                        )
                    }
                }
                else -> {}
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true, widthDp = 392, heightDp = 950)
@Composable
fun RegisterScreenPreview() {
    RegisterScreen(
        onNavigateToLogin = {},
        onRegisterSuccess = {}
    )
}
