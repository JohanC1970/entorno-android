package co.edu.uniquindio.entorno.features.report

import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import co.edu.uniquindio.entorno.R
import co.edu.uniquindio.entorno.core.component.localizedLabel
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import co.edu.uniquindio.entorno.core.component.UriImage
import co.edu.uniquindio.entorno.core.component.icon
import co.edu.uniquindio.entorno.core.theme.EntornoTheme
import co.edu.uniquindio.entorno.core.theme.categoryColors
import co.edu.uniquindio.entorno.core.util.Geo
import co.edu.uniquindio.entorno.core.util.RequestResult
import co.edu.uniquindio.entorno.domain.model.Location
import co.edu.uniquindio.entorno.domain.model.Report
import co.edu.uniquindio.entorno.domain.model.ReportCategory

@Composable
fun CreateReportScreen(
    onClose: () -> Unit = {},
    onPublished: () -> Unit = {},
    onChangeLocation: () -> Unit = {},
    onViewReport: (String) -> Unit = {},
    modifier: Modifier = Modifier,
    viewModel: CreateReportViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    val photoPicker = rememberLauncherForActivityResult(
        ActivityResultContracts.PickMultipleVisualMedia(maxItems = MAX_IMAGES),
        viewModel::onPhotosPicked
    )

    val context = LocalContext.current
    LaunchedEffect(uiState.result) {
        when (val result = uiState.result) {
            is RequestResult.Success -> onPublished()
            is RequestResult.Failure -> {
                snackbarHostState.showSnackbar(result.errorMessage.asString(context))
                viewModel.clearResult()
            }
            else -> Unit
        }
    }

    if (uiState.duplicates.isNotEmpty()) {
        DuplicatesDialog(
            duplicates = uiState.duplicates,
            onView = onViewReport,
            onPublishAnyway = viewModel::publishAnyway,
            onDismiss = viewModel::dismissDuplicates
        )
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
        onRemoveUrl = viewModel::onExistingPhotoRemoved,
        onRemoveUri = viewModel::onPhotoRemoved,
        onClose = onClose,
        onPublish = viewModel::publish,
        onChangeLocation = onChangeLocation,
        modifier = modifier
    )
}

@Composable
private fun DuplicatesDialog(
    duplicates: List<Report>,
    onView: (String) -> Unit,
    onPublishAnyway: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.duplicates_title)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    stringResource(R.string.duplicates_body),
                    style = MaterialTheme.typography.bodyMedium
                )
                duplicates.forEach { report ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                report.title,
                                style = MaterialTheme.typography.titleSmall,
                                maxLines = 2,
                                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                            )
                            Text(
                                stringResource(R.string.comments_category_status, report.category.localizedLabel(), report.neighborhood),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        TextButton(onClick = { onView(report.id) }) { Text(stringResource(R.string.detail_view)) }
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onPublishAnyway) { Text(stringResource(R.string.duplicates_publish_anyway)) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.duplicates_keep_editing)) } }
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
    onRemoveUrl: (String) -> Unit,
    onRemoveUri: (Uri) -> Unit,
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
                title = { Text(stringResource(if (uiState.isEditing) R.string.edit_report_title else R.string.create_title), style = MaterialTheme.typography.titleLarge) },
                navigationIcon = {
                    IconButton(onClick = onClose) {
                        Icon(Icons.Default.Close, contentDescription = stringResource(R.string.action_close))
                    }
                }
            )
        },
        bottomBar = {
            Surface(color = MaterialTheme.colorScheme.background, tonalElevation = 2.dp) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = onPublish,
                        enabled = uiState.canPublish,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                    ) {
                        Text(
                            when {
                                uiState.isLoading -> stringResource(if (uiState.isEditing) R.string.edit_report_saving else R.string.create_publishing)
                                uiState.isEditing -> stringResource(R.string.change_password_save)
                                else -> stringResource(R.string.create_publish)
                            },
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
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
            SectionLabel(stringResource(R.string.create_section_category))
            CategoryGrid(selected = category, onSelect = onCategoryChange)

            Spacer(Modifier.height(4.dp))
            SectionLabel(stringResource(R.string.create_section_details))
            OutlinedTextField(
                value = title,
                onValueChange = onTitleChange,
                singleLine = true,
                placeholder = { Text(stringResource(R.string.create_title_placeholder)) },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = description,
                onValueChange = onDescriptionChange,
                minLines = 4,
                placeholder = { Text(stringResource(R.string.create_description_placeholder)) },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            )
            Text(
                text = stringResource(R.string.create_description_counter, description.length, MAX_DESCRIPTION),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.End,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(4.dp))
            SectionLabel(stringResource(R.string.create_section_photos))
            PhotoRow(
                urls = uiState.existingImageUrls,
                uris = imageUris,
                onAdd = onAddPhotos,
                onRemoveUrl = onRemoveUrl,
                onRemoveUri = onRemoveUri
            )

            Spacer(Modifier.height(4.dp))
            SectionLabel(stringResource(R.string.create_section_location))
            LocationCard(
                location = uiState.location,
                onChangeLocation = onChangeLocation
            )
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
                repeat(3 - rowItems.size) {
                    Spacer(Modifier.weight(1f))
                }
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
                text = category.localizedLabel(),
                style = MaterialTheme.typography.labelMedium,
                color = if (isSelected) colors.chipText else MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )
        }
        if (isSelected) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = stringResource(R.string.create_selected),
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
    urls: List<String>,
    uris: List<Uri>,
    onAdd: () -> Unit,
    onRemoveUrl: (String) -> Unit,
    onRemoveUri: (Uri) -> Unit
) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        urls.forEach { url -> PhotoThumbnail(model = url, onRemove = { onRemoveUrl(url) }) }
        uris.forEach { uri -> PhotoThumbnail(model = uri.toString(), onRemove = { onRemoveUri(uri) }) }
        if (urls.size + uris.size < MAX_IMAGES) AddPhotoBox(onClick = onAdd)
    }
}

@Composable
private fun PhotoThumbnail(model: String, onRemove: () -> Unit) {
    Box(modifier = Modifier.size(80.dp)) {
        UriImage(
            uri = model,
            modifier = Modifier
                .matchParentSize()
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
        )
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
                contentDescription = stringResource(R.string.create_remove_photo),
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
            text = stringResource(R.string.create_add_photo),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun LocationCard(location: Location?, onChangeLocation: () -> Unit) {
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
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 12.dp)
            ) {
                Text(
                    text = stringResource(if (location == null) R.string.create_choose_where else R.string.create_location_chosen),
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = location?.let(Geo::format) ?: stringResource(R.string.create_tap_choose),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            TextButton(onClick = onChangeLocation) {
                Text(stringResource(if (location == null) R.string.create_choose else R.string.create_change_location))
            }
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
            onRemoveUrl = {},
            onRemoveUri = {},
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
