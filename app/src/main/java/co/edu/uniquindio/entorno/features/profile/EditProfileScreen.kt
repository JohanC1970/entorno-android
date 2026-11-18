package co.edu.uniquindio.entorno.features.profile

import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import co.edu.uniquindio.entorno.R
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.material3.TextButton
import androidx.compose.material3.AlertDialog
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import co.edu.uniquindio.entorno.core.theme.EntornoTheme

@Composable
fun EditProfileScreen(
    onBackClick: () -> Unit = {},
    viewModel: EditProfileViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    val context = LocalContext.current
    LaunchedEffect(state.guardadoExitoso) {
        if (state.guardadoExitoso) {
            snackbarHostState.showSnackbar(context.getString(R.string.edit_profile_saved))
            viewModel.mensajeMostrado()
        }
    }
    LaunchedEffect(state.mensajeError) {
        state.mensajeError?.let {
            snackbarHostState.showSnackbar(it.asString(context))
            viewModel.mensajeMostrado()
        }
    }

    EditProfileContent(
        state = state,
        snackbarHostState = snackbarHostState,
        onNombreChange = viewModel::actualizarNombre,
        onTelefonoChange = viewModel::actualizarTelefono,
        onBarrioChange = viewModel::actualizarBarrio,
        onGuardarClick = viewModel::guardarCambios,
        onEliminarConfirmado = viewModel::eliminarCuenta,
        onBackClick = onBackClick
    )
}

@Composable
private fun EditProfileContent(
    state: EditProfileUiState,
    snackbarHostState: SnackbarHostState,
    onNombreChange: (String) -> Unit,
    onTelefonoChange: (String) -> Unit,
    onBarrioChange: (String) -> Unit,
    onGuardarClick: () -> Unit,
    onEliminarConfirmado: (String) -> Unit,
    onBackClick: () -> Unit
) {
    var mostrarDialogoEliminar by remember { mutableStateOf(false) }
    var contrasenaEliminar by remember { mutableStateOf("") }

    if (mostrarDialogoEliminar) {
        AlertDialog(
            onDismissRequest = { mostrarDialogoEliminar = false },
            title = { Text(stringResource(R.string.edit_profile_delete_title)) },
            text = {
                Column {
                    Text(stringResource(R.string.edit_profile_delete_body))
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = contrasenaEliminar,
                        onValueChange = { contrasenaEliminar = it },
                        label = { Text(stringResource(R.string.field_password)) },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation()
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        mostrarDialogoEliminar = false
                        onEliminarConfirmado(contrasenaEliminar)
                        contrasenaEliminar = ""
                    },
                    enabled = !state.eliminando
                ) { Text(stringResource(R.string.action_delete), color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = {
                    mostrarDialogoEliminar = false
                    contrasenaEliminar = ""
                }) { Text(stringResource(R.string.action_cancel)) }
            }
        )
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets(0),
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding),
        contentPadding = PaddingValues(bottom = 32.dp)
    ) {
        // Encabezado con flecha atrás y título
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(start = 8.dp, end = 20.dp, top = 8.dp, bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.action_back),
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                }
                Text(
                    text = stringResource(R.string.edit_profile_title),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
        }

        // Avatar con botón de cambiar foto
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(contentAlignment = Alignment.BottomEnd) {
                    Box(
                        modifier = Modifier
                            .size(88.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = state.iniciales,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .offset(x = (-2).dp, y = (-2).dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.CameraAlt,
                            contentDescription = stringResource(R.string.edit_profile_change_photo),
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = stringResource(R.string.edit_profile_change_photo),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.clickable { /* Sin acción en Fase 2 */ }
                )
            }
        }

        // Campo: Nombre completo
        item {
            CampoEditar(
                etiqueta = stringResource(R.string.field_full_name),
                valor = state.nombreCompleto,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp),
                onValueChange = onNombreChange,
                error = state.nombreError?.asString()
            )
        }

        // Campo: Correo electrónico (no editable)
        item {
            CampoEditar(
                etiqueta = stringResource(R.string.edit_profile_email_readonly),
                valor = state.correo,
                onValueChange = {},
                enabled = false,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)
            )
        }

        // Campo: Teléfono
        item {
            CampoEditar(
                etiqueta = stringResource(R.string.edit_profile_phone),
                valor = state.telefono,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp),
                onValueChange = onTelefonoChange
            )
        }

        // Campo: Barrio / zona
        item {
            CampoEditar(
                etiqueta = stringResource(R.string.edit_profile_neighborhood),
                valor = state.barrio,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp),
                onValueChange = onBarrioChange
            )
        }

        // Botón Guardar cambios
        item {
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = onGuardarClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .height(52.dp),
                shape = RoundedCornerShape(26.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                ),
                enabled = !state.guardando && !state.eliminando
            ) {
                Text(
                    text = stringResource(R.string.change_password_save),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        // Botón Eliminar cuenta
        item {
            Spacer(modifier = Modifier.height(12.dp))
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(R.string.edit_profile_delete_title),
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.error,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.clickable { mostrarDialogoEliminar = true }
                )
            }
        }
    }
    }
}

@Composable
private fun CampoEditar(
    etiqueta: String,
    valor: String,
    modifier: Modifier = Modifier,
    onValueChange: (String) -> Unit = {},
    error: String? = null,
    enabled: Boolean = true
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = etiqueta,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 6.dp)
        )
        OutlinedTextField(
            value = valor,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                focusedContainerColor = MaterialTheme.colorScheme.surface
            ),
            singleLine = true,
            enabled = enabled,
            isError = error != null,
            supportingText = { if (error != null) Text(error) }
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun EditProfileScreenPreview() {
    EntornoTheme {
        EditProfileContent(
            snackbarHostState = remember { SnackbarHostState() },
            state = EditProfileUiState(
                nombreCompleto = "Johan García",
                correo = "johanc.garciag@uqvirtual.edu.co",
                telefono = "+57 300 000 0000",
                barrio = "Ej. La Fachada, Armenia",
                iniciales = "JG"
            ),
            onNombreChange = {},
            onTelefonoChange = {},
            onBarrioChange = {},
            onGuardarClick = {},
            onEliminarConfirmado = {},
            onBackClick = {}
        )
    }
}
