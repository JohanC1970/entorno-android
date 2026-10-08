package co.edu.uniquindio.entorno.features.report

import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import co.edu.uniquindio.entorno.core.util.RequestResult
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import co.edu.uniquindio.entorno.core.component.icon
import co.edu.uniquindio.entorno.core.theme.EntornoTheme
import co.edu.uniquindio.entorno.core.theme.categoryColors
import co.edu.uniquindio.entorno.domain.model.ReportCategory

@Composable
fun CreateReportScreen(
    onClose: () -> Unit = {},
    onPublished: () -> Unit = {},
    onChangeLocation: () -> Unit = {},
    modifier: Modifier = Modifier,
    viewModel: CreateReportViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    val photoPicker = rememberLauncherForActivityResult(
        ActivityResultContracts.PickMultipleVisualMedia(maxItems = MAX_IMAGES),
        viewModel::onPhotosPicked
    )

    LaunchedEffect(uiState.result) {
        when (val result = uiState.result) {
            is RequestResult.Success -> onPublished()
            is RequestResult.Failure -> {
                snackbarHostState.showSnackbar(result.errorMessage)
                viewModel.clearResult()
            }
            else -> Unit
        }
    }

    CreateReportContent(
        uiState = uiState,
        snackbarHostState = snackbarHostState,
        onCategoryChange = viewModel::onCategoryChange,
        onTitleChange = viewModel::onTitleChange,
        onDescriptionChange = viewModel::onDescriptionChange,
        onAddPhotos = {
            photoPicker.launch(
                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
            )
        },
        onRemovePhoto = viewModel::onPhotoRemoved,
        onClose = onClose,
        onPublish = viewModel::publish,
        onChangeLocation = onChangeLocation,
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CreateReportContent(
    uiState: CreateReportUiState,
    snackbarHostState: SnackbarHostState,
    onCategoryChange: (ReportCategory) -> Unit,
    onTitleChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onAddPhotos: () -> Unit,
    onRemovePhoto: (Uri) -> Unit,
    onClose: () -> Unit,
    onPublish: () -> Unit,
    onChangeLocation: () -> Unit,
    modifier: Modifier = Modifier
) {
    val category = uiState.category
    val title = uiState.title
    val description = uiState.description
    val imageUris = uiState.imageUris

    Scaffold(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Crear reporte", style = MaterialTheme.typography.titleLarge) },
                navigationIcon = {
                    IconButton(onClick = onClose) {
                        Icon(Icons.Default.Close, contentDescription = "Cerrar")
                    }
                }
            )
        },
        bottomBar = {
            Surface(color = MaterialTheme.colorScheme.background, tonalElevation = 2.dp) {
                Button(
                    onClick = onPublish,
                    enabled = uiState.canPublish,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                        .height(56.dp)
                ) {
                    Text(if (uiState.isLoading) "Publicando…" else "Publicar reporte", style = MaterialTheme.typography.titleMedium)
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            SectionLabel("CATEGORÍA")
            CategoryGrid(selected = category, onSelect = onCategoryChange)

            Spacer(Modifier.height(4.dp))
            SectionLabel("DETALLES")
            OutlinedTextField(
                value = title,
                onValueChange = onTitleChange,
                singleLine = true,
                placeholder = { Text("¿Qué está pasando?") },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = description,
                onValueChange = onDescriptionChange,
                minLines = 4,
                placeholder = { Text("Describe qué pasó, desde cuándo y a quién afecta") },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            )
            Text(
                text = "${description.length} / $MAX_DESCRIPTION",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.End,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(4.dp))
            SectionLabel("FOTOS · MÍNIMO UNA")
            PhotoRow(uris = imageUris, onAdd = onAddPhotos, onRemove = onRemovePhoto)

            Spacer(Modifier.height(4.dp))
            SectionLabel("UBICACIÓN")
            LocationCard(onChangeLocation = onChangeLocation)
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

@Composable
private fun CategoryGrid(
    selected: ReportCategory?,
    onSelect: (ReportCategory) -> Unit
) {
    // Cuadrícula de 3 columnas dentro de un scroll vertical: filas manuales, sin lazy anidado.
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        ReportCategory.entries.chunked(3).forEach { rowItems ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                rowItems.forEach { item ->
                    CategoryCell(
                        category = item,
                        isSelected = item == selected,
                        onClick = { onSelect(item) },
                        modifier = Modifier.weight(1f)
                    )
                }
                repeat(3 - rowItems.size) { Spacer(Modifier.weight(1f)) }
            }
        }
    }
}

@Composable
private fun CategoryCell(
    category: ReportCategory,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = categoryColors(category)
    val shape = RoundedCornerShape(12.dp)
    val background = if (isSelected) colors.chipBackground else MaterialTheme.colorScheme.surface
    val borderColor = if (isSelected) colors.icon else MaterialTheme.colorScheme.outlineVariant

    Box(
        modifier = modifier
            .aspectRatio(1f)
            .clip(shape)
            .background(background)
            .border(BorderStroke(if (isSelected) 2.dp else 1.dp, borderColor), shape)
            .clickable(onClick = onClick)
            .padding(8.dp)
    ) {
        Column(
            modifier = Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = category.icon(),
                contentDescription = null,
                tint = colors.icon,
                modifier = Modifier.size(28.dp)
            )
            Text(
                text = category.label,
                style = MaterialTheme.typography.labelMedium,
                color = if (isSelected) colors.chipText else MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )
        }
        if (isSelected) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = "Seleccionada",
                tint = colors.icon,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(18.dp)
            )
        }
    }
}

@Composable
private fun PhotoRow(
    uris: List<Uri>,
    onAdd: () -> Unit,
    onRemove: (Uri) -> Unit
) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        uris.forEach { uri ->
            PhotoThumbnail(uri = uri, onRemove = { onRemove(uri) })
        }
        if (uris.size < MAX_IMAGES) {
            AddPhotoBox(onClick = onAdd)
        }
    }
}

@Composable
private fun PhotoThumbnail(uri: Uri, onRemove: () -> Unit) {
    val context = LocalContext.current
    // TODO: simplificar con Coil AsyncImage cuando se agregue
    val bitmap = remember(uri) {
        context.contentResolver.openInputStream(uri)?.use {
            BitmapFactory.decodeStream(it)?.asImageBitmap()
        }
    }
    val shape = RoundedCornerShape(12.dp)

    Box(modifier = Modifier.size(80.dp)) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .clip(shape)
                .background(MaterialTheme.colorScheme.surfaceVariant)
        ) {
            bitmap?.let {
                Image(
                    bitmap = it,
                    contentDescription = "Foto del reporte",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.matchParentSize()
                )
            }
        }
        IconButton(
            onClick = onRemove,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(2.dp)
                .size(22.dp)
                .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.6f), CircleShape)
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Quitar foto",
                tint = MaterialTheme.colorScheme.surface,
                modifier = Modifier.size(14.dp)
            )
        }
    }
}

@Composable
private fun AddPhotoBox(onClick: () -> Unit) {
    val dashColor = MaterialTheme.colorScheme.outline
    Column(
        modifier = Modifier
            .size(80.dp)
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .drawBehind {
                drawRoundRect(
                    color = dashColor,
                    cornerRadius = CornerRadius(12.dp.toPx()),
                    style = Stroke(
                        width = 1.5.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(16f, 12f))
                    )
                )
            },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.CameraAlt,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = "Añadir",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun LocationCard(onChangeLocation: () -> Unit) {
    // TODO: ubicación real cuando se integre el mapa
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(start = 16.dp, top = 12.dp, bottom = 12.dp, end = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.LocationOn,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
            Column(modifier = Modifier
                .weight(1f)
                .padding(horizontal = 12.dp)) {
                Text(
                    text = "Ubicación actual detectada",
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = "4.5339° N, 75.6811° W",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            TextButton(onClick = onChangeLocation) { Text("Cambiar") }
        }
    }
}

private val previewUris = listOf(Uri.parse("content://preview/1"), Uri.parse("content://preview/2"))

@Composable
private fun PreviewContent(uiState: CreateReportUiState) {
    EntornoTheme {
        CreateReportContent(
            uiState = uiState,
            snackbarHostState = remember { SnackbarHostState() },
            onCategoryChange = {},
            onTitleChange = {},
            onDescriptionChange = {},
            onAddPhotos = {},
            onRemovePhoto = {},
            onClose = {},
            onPublish = {},
            onChangeLocation = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun CreateReportEmptyPreview() = PreviewContent(CreateReportUiState())

@Preview(showBackground = true)
@Composable
private fun CreateReportFilledPreview() = PreviewContent(
    CreateReportUiState(
        category = ReportCategory.INFRASTRUCTURE,
        title = "Hueco grande en la carrera 14",
        description = "Lleva dos semanas y ya causó un accidente a un motociclista.",
        imageUris = previewUris
    )
)

@Preview(showBackground = true, uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun CreateReportDarkPreview() = PreviewContent(
    CreateReportUiState(category = ReportCategory.PETS, title = "Perro perdido")
)
