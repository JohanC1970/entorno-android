package co.edu.uniquindio.entorno.features.auth

import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import co.edu.uniquindio.entorno.R
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.LocationCity
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
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
import co.edu.uniquindio.entorno.features.map.LocationPickerDialog

@Composable
fun RegisterScreen(
    onNavigateToLogin: () -> Unit,
    onRegisterSuccess: () -> Unit,
    onNavigateToTerms: () -> Unit = {},
    onNavigateToPrivacy: () -> Unit = {},
    viewModel: RegisterViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }
    var showLocationPicker by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }

    val context = LocalContext.current
    LaunchedEffect(uiState.registerResult) {
        when (val result = uiState.registerResult) {
            is RequestResult.Success -> {
                snackbarHostState.showSnackbar(result.message.asString(context))
                viewModel.clearResult()
                onRegisterSuccess()
            }
            is RequestResult.Failure -> {
                snackbarHostState.showSnackbar(result.errorMessage.asString(context))
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
            Spacer(modifier = Modifier.height(16.dp))

            AuthHeader()

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = stringResource(R.string.register_title),
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = stringResource(R.string.register_subtitle),
                fontSize = 15.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
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
                label = stringResource(R.string.field_full_name),
                placeholder = "Andres Osorio",
                leadingIcon = Icons.Default.Person,
                errorMessage = uiState.nameError?.asString()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Correo electrónico
            AppTextField(
                value = uiState.email,
                onValueChange = { viewModel.onEmailChange(it) },
                label = stringResource(R.string.field_email),
                placeholder = stringResource(R.string.field_email_placeholder),
                leadingIcon = Icons.Default.Email,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                errorMessage = uiState.emailError?.asString()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Botón de ubicación
            OutlinedButton(
                onClick = { showLocationPicker = true },
                modifier = Modifier.fillMaxWidth().height(54.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Default.MyLocation, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (uiState.location == null) stringResource(R.string.register_pick_location)
                    else stringResource(R.string.register_location_chosen),
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Ciudad
            AppTextField(
                value = uiState.city,
                onValueChange = { viewModel.onCityChange(it) },
                label = stringResource(R.string.field_city),
                placeholder = "Armenia",
                leadingIcon = Icons.Default.LocationCity,
                errorMessage = uiState.cityError?.asString()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Dirección
            AppTextField(
                value = uiState.address,
                onValueChange = { viewModel.onAddressChange(it) },
                label = stringResource(R.string.field_address),
                placeholder = stringResource(R.string.field_address_placeholder),
                leadingIcon = Icons.Default.Place,
                errorMessage = uiState.addressError?.asString()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Contraseña
            AppTextField(
                value = uiState.password,
                onValueChange = { viewModel.onPasswordChange(it) },
                label = stringResource(R.string.field_password),
                placeholder = "••••••••",
                leadingIcon = Icons.Default.Lock,
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                errorMessage = uiState.passwordError?.asString(),
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = stringResource(if (passwordVisible) R.string.password_hide else R.string.password_show),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Confirmar contraseña
            AppTextField(
                value = uiState.confirmPassword,
                onValueChange = { viewModel.onConfirmPasswordChange(it) },
                label = stringResource(R.string.field_confirm_password),
                placeholder = "••••••••",
                leadingIcon = Icons.Default.Lock,
                visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                errorMessage = uiState.confirmPasswordError?.asString(),
                trailingIcon = {
                    IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                        Icon(
                            imageVector = if (confirmPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = stringResource(if (confirmPasswordVisible) R.string.password_hide else R.string.password_show),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Texto legal interactivo
            val linkColor = MaterialTheme.colorScheme.primary
            val legalText = buildAnnotatedString {
                append(stringResource(R.string.legal_register_prefix))
                pushStringAnnotation(tag = "terms", annotation = "terms")
                withStyle(
                    style = SpanStyle(
                        color = linkColor,
                        fontWeight = FontWeight.SemiBold,
                        textDecoration = TextDecoration.Underline
                    )
                ) {
                    append(stringResource(R.string.legal_terms))
                }
                pop()
                append(stringResource(R.string.legal_and))
                pushStringAnnotation(tag = "privacy", annotation = "privacy")
                withStyle(
                    style = SpanStyle(
                        color = linkColor,
                        fontWeight = FontWeight.SemiBold,
                        textDecoration = TextDecoration.Underline
                    )
                ) {
                    append(stringResource(R.string.legal_privacy))
                }
                pop()
                append(".")
            }

            ClickableText(
                text = legalText,
                style = LocalTextStyle.current.copy(
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                onClick = { offset ->
                    legalText.getStringAnnotations(tag = "terms", start = offset, end = offset)
                        .firstOrNull()?.let { onNavigateToTerms() }
                    legalText.getStringAnnotations(tag = "privacy", start = offset, end = offset)
                        .firstOrNull()?.let { onNavigateToPrivacy() }
                }
            )

            Spacer(modifier = Modifier.height(28.dp))

            Button(
                onClick = { viewModel.register() },
                enabled = uiState.isFormValid && uiState.registerResult !is RequestResult.Loading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                if (uiState.registerResult is RequestResult.Loading) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(24.dp))
                } else {
                    Text(
                        text = stringResource(R.string.register_button),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }

        if (showLocationPicker) {
            LocationPickerDialog(
                initial = uiState.location,
                onDismiss = { showLocationPicker = false },
                onConfirm = { place ->
                    viewModel.onPlaceSelected(place)
                    showLocationPicker = false
                }
            )
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
