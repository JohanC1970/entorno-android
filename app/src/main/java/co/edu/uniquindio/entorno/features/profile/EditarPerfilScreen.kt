package co.edu.uniquindio.entorno.features.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import co.edu.uniquindio.entorno.core.theme.*

@Composable
fun EditarPerfilScreen(
    viewModel: EditarPerfilViewModel,
    onBackClick: () -> Unit = {}
) {
    val state by viewModel.uiState.collectAsState()

    EditarPerfilContent(
        state = state,
        onNombreChange = viewModel::actualizarNombre,
        onCorreoChange = viewModel::actualizarCorreo,
        onTelefonoChange = viewModel::actualizarTelefono,
        onBarrioChange = viewModel::actualizarBarrio,
        onGuardarClick = viewModel::guardarCambios,
        onEliminarClick = viewModel::eliminarCuenta,
        onBackClick = onBackClick
    )
}

@Composable
private fun EditarPerfilContent(
    state: EditarPerfilUiState,
    onNombreChange: (String) -> Unit,
    onCorreoChange: (String) -> Unit,
    onTelefonoChange: (String) -> Unit,
    onBarrioChange: (String) -> Unit,
    onGuardarClick: () -> Unit,
    onEliminarClick: () -> Unit,
    onBackClick: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(bottom = 32.dp)
    ) {
        // Encabezado con flecha atrás y título
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 8.dp, end = 20.dp, top = 40.dp, bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Volver",
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                }
                Text(
                    text = "Editar perfil",
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
                            .background(ContenedorPrimarioClaro),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = state.iniciales,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = PrimarioClaro
                        )
                    }
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .offset(x = (-2).dp, y = (-2).dp)
                            .clip(CircleShape)
                            .background(PrimarioClaro),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.CameraAlt,
                            contentDescription = "Cambiar foto",
                            tint = OnPrimarioClaro,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Cambiar foto",
                    style = MaterialTheme.typography.bodyMedium,
                    color = PrimarioClaro,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.clickable { /* Sin acción en Fase 2 */ }
                )
            }
        }

        // Campo: Nombre completo
        item {
            CampoEditar(
                etiqueta = "Nombre completo",
                valor = state.nombreCompleto,
                onValueChange = onNombreChange,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)
            )
        }

        // Campo: Correo electrónico
        item {
            CampoEditar(
                etiqueta = "Correo electrónico",
                valor = state.correo,
                onValueChange = onCorreoChange,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)
            )
        }

        // Campo: Teléfono
        item {
            CampoEditar(
                etiqueta = "Teléfono",
                valor = state.telefono,
                onValueChange = onTelefonoChange,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)
            )
        }

        // Campo: Barrio / zona
        item {
            CampoEditar(
                etiqueta = "Barrio / zona",
                valor = state.barrio,
                onValueChange = onBarrioChange,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)
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
                enabled = !state.guardando
            ) {
                Text(
                    text = "Guardar cambios",
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
                    text = "Eliminar mi cuenta",
                    style = MaterialTheme.typography.titleSmall,
                    color = SeveridadAlta,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.clickable(onClick = onEliminarClick)
                )
            }
        }
    }
}

@Composable
private fun CampoEditar(
    etiqueta: String,
    valor: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
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
            singleLine = true
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun EditarPerfilScreenPreview() {
    EntornoTheme {
        EditarPerfilContent(
            state = EditarPerfilUiState(
                nombreCompleto = "Johan García",
                correo = "johanc.garciag@uqvirtual.edu.co",
                telefono = "+57 300 000 0000",
                barrio = "Ej. La Fachada, Armenia",
                iniciales = "JG"
            ),
            onNombreChange = {},
            onCorreoChange = {},
            onTelefonoChange = {},
            onBarrioChange = {},
            onGuardarClick = {},
            onEliminarClick = {},
            onBackClick = {}
        )
    }
}
