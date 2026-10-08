package co.edu.uniquindio.entorno.features.report

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import co.edu.uniquindio.entorno.R
import co.edu.uniquindio.entorno.core.component.CategoryChip
import co.edu.uniquindio.entorno.core.component.SeverityBar
import co.edu.uniquindio.entorno.core.component.StatusChip
import co.edu.uniquindio.entorno.core.component.icon
import co.edu.uniquindio.entorno.core.theme.EntornoTheme
import co.edu.uniquindio.entorno.core.theme.categoryColors
import co.edu.uniquindio.entorno.core.theme.statusColors
import co.edu.uniquindio.entorno.domain.model.Location
import co.edu.uniquindio.entorno.domain.model.Report
import co.edu.uniquindio.entorno.domain.model.ReportStatus
import co.edu.uniquindio.entorno.features.home.SampleReports
import java.time.format.DateTimeFormatter
import java.util.Locale

private val dateFormatter = DateTimeFormatter.ofPattern("d MMM yyyy", Locale.forLanguageTag("es"))

private data class Milestone(val label: String, val reached: Boolean, val date: String? = null)

private fun Location.formatted(): String =
    String.format(Locale.US, "%.4f, %.4f", latitude, longitude)

private fun Report.milestones(): List<Milestone> {
    val reachedCount = when (status) {
        ReportStatus.PENDING -> 1
        ReportStatus.VERIFIED -> 2
        ReportStatus.RESOLVED -> 3
        ReportStatus.REJECTED -> 1
    }
    val secondLabel = when (status) {
        ReportStatus.REJECTED -> "Rechazado"
        ReportStatus.PENDING, ReportStatus.VERIFIED, ReportStatus.RESOLVED -> "Verificado"
    }
    // TODO: fechas por hito cuando Report tenga verifiedAt y resolvedAt
    return listOf(
        Milestone("Reportado", reachedCount >= 1, date.format(dateFormatter).replace(".", "")),
        Milestone(secondLabel, reachedCount >= 2),
        Milestone("Resuelto", reachedCount >= 3)
    )
}

@Composable
fun ReportDetailScreen(
    onBack: () -> Unit = {},
    onShare: () -> Unit = {},
    onCommentsClick: (reportId: String) -> Unit = {},
    onViewOnMap: () -> Unit = {},
    viewModel: ReportDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    when (val report = uiState.report) {
        null -> ReportDetailPlaceholder(
            isLoading = uiState.isLoading,
            snackbarHostState = snackbarHostState,
            onBack = onBack
        )
        else -> ReportDetailContent(
            report = report,
            isImportant = uiState.isImportant,
            snackbarHostState = snackbarHostState,
            onBack = onBack,
            onToggleImportant = viewModel::toggleImportant,
            onShare = onShare,
            onCommentsClick = { onCommentsClick(report.id) },
            onViewOnMap = onViewOnMap
        )
    }
}

@Composable
private fun ReportDetailPlaceholder(
    isLoading: Boolean,
    snackbarHostState: SnackbarHostState,
    onBack: () -> Unit
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (isLoading) {
                CircularProgressIndicator()
            } else {
                Text(
                    text = "No encontramos este reporte",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.height(16.dp))
                OutlinedButton(onClick = onBack) { Text("Volver") }
            }
        }
    }
}

@Composable
fun ReportDetailContent(
    report: Report,
    isImportant: Boolean = false,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    onBack: () -> Unit = {},
    onToggleImportant: () -> Unit = {},
    onShare: () -> Unit = {},
    onCommentsClick: () -> Unit = {},
    onViewOnMap: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets(0),
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            ImageHeader(report = report, onBack = onBack)

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .navigationBarsPadding(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    StatusChip(report.status)
                    if (report.status == ReportStatus.VERIFIED) {
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = "por un moderador",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Text(
                    text = report.title,
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )

                SeverityBar(report.severity)

                ActionsRow(
                    report = report,
                    isImportant = isImportant,
                    onToggleImportant = onToggleImportant,
                    onShare = onShare,
                    onCommentsClick = onCommentsClick
                )

                Text(
                    text = report.description,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )

                LocationCard(location = report.location, onViewOnMap = onViewOnMap)

                if (report.status == ReportStatus.REJECTED) {
                    report.rejectionReason?.let { reason -> RejectionCard(reason) }
                }

                Text(
                    text = "Seguimiento",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Timeline(report.milestones())
            }
        }
    }
}

@Composable
private fun ImageHeader(report: Report, onBack: () -> Unit) {
    val colors = categoryColors(report.category)
    // TODO: cargar imagenUrls reales cuando se agregue Coil
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(236.dp)
            .background(colors.chipBackground)
    ) {
        Icon(
            imageVector = report.category.icon(),
            contentDescription = null,
            tint = colors.icon,
            modifier = Modifier
                .align(Alignment.Center)
                .size(96.dp)
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(12.dp)
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier.align(Alignment.TopStart),
                colors = IconButtonDefaults.iconButtonColors(
                    containerColor = MaterialTheme.colorScheme.scrim.copy(alpha = 0.45f),
                    contentColor = Color.White
                )
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Volver"
                )
            }

            Box(Modifier.align(Alignment.TopEnd)) {
                CategoryChip(report.category)
            }

            if (report.imageUrls.isNotEmpty()) {
                Surface(
                    shape = RoundedCornerShape(50),
                    color = MaterialTheme.colorScheme.scrim.copy(alpha = 0.6f),
                    modifier = Modifier.align(Alignment.BottomEnd)
                ) {
                    Text(
                        text = "1 / ${report.imageUrls.size}",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun ActionsRow(
    report: Report,
    isImportant: Boolean,
    onToggleImportant: () -> Unit,
    onShare: () -> Unit,
    onCommentsClick: () -> Unit
) {
    val buttonShape = RoundedCornerShape(12.dp)
    val outline = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    val importantContent: @Composable () -> Unit = {
        Icon(
            painter = painterResource(R.drawable.ic_flag),
            contentDescription = null,
            modifier = Modifier.size(18.dp)
        )
        Spacer(Modifier.width(8.dp))
        Text("Es importante · ${report.importantCount}")
    }
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val importantModifier = Modifier
            .weight(1f)
            .height(48.dp)
        if (isImportant) {
            Button(
                onClick = onToggleImportant,
                modifier = importantModifier,
                shape = buttonShape,
                content = { importantContent() }
            )
        } else {
            OutlinedButton(
                onClick = onToggleImportant,
                modifier = importantModifier,
                shape = buttonShape,
                border = outline,
                content = { importantContent() }
            )
        }

        OutlinedButton(
            onClick = onShare,
            modifier = Modifier.size(48.dp),
            shape = buttonShape,
            contentPadding = PaddingValues(0.dp),
            border = outline
        ) {
            Icon(
                imageVector = Icons.Default.Share,
                contentDescription = "Compartir",
                modifier = Modifier.size(20.dp)
            )
        }

        OutlinedButton(
            onClick = onCommentsClick,
            modifier = Modifier
                .height(48.dp)
                .defaultMinSize(minWidth = 48.dp),
            shape = buttonShape,
            contentPadding = PaddingValues(horizontal = 12.dp),
            border = outline
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_comment),
                contentDescription = "Comentarios",
                modifier = Modifier.size(18.dp)
            )
            Spacer(Modifier.width(6.dp))
            Text("${report.commentCount}")
        }
    }
}

@Composable
private fun LocationCard(location: Location, onViewOnMap: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.LocationOn,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.width(12.dp))
            // TODO: mostrar la dirección cuando Report tenga ese campo
            Text(
                text = location.formatted(),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = "Ver",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.clickable(onClick = onViewOnMap)
            )
        }
    }
}

@Composable
private fun RejectionCard(reason: String) {
    val colors = statusColors(ReportStatus.REJECTED)
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = colors.background,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = "Motivo del rechazo",
                style = MaterialTheme.typography.labelLarge,
                color = colors.text
            )
            Text(
                text = reason,
                style = MaterialTheme.typography.bodyMedium,
                color = colors.text
            )
        }
    }
}

@Composable
private fun Timeline(milestones: List<Milestone>) {
    Column {
        milestones.forEachIndexed { index, milestone ->
            val isLast = index == milestones.lastIndex
            val nextReached = milestones.getOrNull(index + 1)?.reached == true
            Row {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    MilestoneDot(reached = milestone.reached)
                    if (!isLast) {
                        Box(
                            modifier = Modifier
                                .width(2.dp)
                                .height(32.dp)
                                .background(
                                    if (nextReached) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.outlineVariant
                                )
                        )
                    }
                }
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(
                        text = milestone.label,
                        style = MaterialTheme.typography.bodyLarge,
                        color = if (milestone.reached) MaterialTheme.colorScheme.onSurface
                        else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    milestone.date?.let { date ->
                        Text(
                            text = date,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MilestoneDot(reached: Boolean) {
    if (reached) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .background(MaterialTheme.colorScheme.primary, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(14.dp)
            )
        }
    } else {
        Box(
            modifier = Modifier
                .size(24.dp)
                .border(2.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape)
        )
    }
}

private const val SAMPLE_DESCRIPTION =
    "Llevamos varias semanas con este problema y nadie ha respondido. " +
        "Pedimos a la comunidad que lo apoye para que sea atendido."

@Preview(showBackground = true)
@Composable
private fun ReportDetailVerifiedPreview() {
    EntornoTheme {
        ReportDetailContent(
            report = SampleReports.reports[0].copy(
                description = SAMPLE_DESCRIPTION,
                imageUrls = listOf("a", "b", "c")
            ),
            isImportant = false
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ReportDetailRejectedPreview() {
    EntornoTheme {
        ReportDetailContent(
            report = SampleReports.reports[3].copy(
                description = SAMPLE_DESCRIPTION,
                rejectionReason = "El reporte no corresponde a una problemática de la comunidad."
            )
        )
    }
}

@Preview(showBackground = true, uiMode = UI_MODE_NIGHT_YES)
@Composable
private fun ReportDetailVerifiedDarkPreview() {
    EntornoTheme {
        ReportDetailContent(
            report = SampleReports.reports[0].copy(
                description = SAMPLE_DESCRIPTION,
                imageUrls = listOf("a", "b", "c")
            ),
            isImportant = true
        )
    }
}
