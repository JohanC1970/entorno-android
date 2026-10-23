package co.edu.uniquindio.entorno.features.map

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import co.edu.uniquindio.entorno.core.component.CategoryChip
import co.edu.uniquindio.entorno.core.component.StatusChip
import co.edu.uniquindio.entorno.core.util.Geo
import co.edu.uniquindio.entorno.domain.model.Report
import co.edu.uniquindio.entorno.domain.model.ReportCategory
import kotlinx.coroutines.launch

@Composable
fun MapScreen(
    onReportClick: (String) -> Unit,
    onHeatMapClick: () -> Unit,
    viewModel: MapViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val mapState = rememberMapViewState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var query by remember { mutableStateOf("") }

    // Al abrir el mapa desde un reporte (o al tocar un marcador) se centra en él.
    val selected = state.selectedReport
    LaunchedEffect(mapState.isReady, selected?.id) {
        if (mapState.isReady && selected != null) mapState.centerOn(selected.location, 3f)
    }

    fun search() {
        val text = query.trim()
        val neighborhood = Geo.neighborhoods.firstOrNull { it.name.contains(text, ignoreCase = true) }
        when {
            text.isEmpty() -> Unit
            neighborhood != null -> mapState.centerOn(neighborhood.center, 2.5f)
            else -> scope.launch { snackbarHostState.showSnackbar("No encontramos ese barrio") }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        MapSurface(
            modifier = Modifier.fillMaxSize(),
            state = mapState,
            userLocation = state.userLocation,
            alertRadiusMeters = state.radiusKm * 1000.0,
            onMapTap = { viewModel.onReportSelected(null) }
        ) {
            state.reports.forEach { report ->
                key(report.id) {
                    MapPin(
                        category = report.category,
                        location = report.location,
                        state = mapState,
                        selected = report.id == selected?.id,
                        onClick = { viewModel.onReportSelected(report.id) }
                    )
                }
            }
        }

        Column(
            modifier = Modifier.align(Alignment.TopCenter).statusBarsPadding().padding(horizontal = 12.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                placeholder = { Text("Buscar barrio") },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                trailingIcon = {
                    IconButton(onClick = onHeatMapClick) {
                        Icon(
                            imageVector = Icons.Filled.LocalFireDepartment,
                            contentDescription = "Mapa de calor",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = { search() }),
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface
                ),
                modifier = Modifier.fillMaxWidth()
            )
            CategoryFilterRow(selected = state.selectedCategory, onSelected = viewModel::onCategorySelected)
        }

        Column(
            modifier = Modifier.align(Alignment.CenterEnd).padding(end = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SmallFloatingActionButton(onClick = { mapState.zoomBy(1.4f) }) {
                Icon(Icons.Filled.Add, contentDescription = "Acercar")
            }
            SmallFloatingActionButton(onClick = { mapState.zoomBy(1f / 1.4f) }) {
                Icon(Icons.Filled.Remove, contentDescription = "Alejar")
            }
            SmallFloatingActionButton(onClick = { mapState.centerOn(state.userLocation, 2f) }) {
                Icon(Icons.Filled.MyLocation, contentDescription = "Mi ubicación")
            }
        }

        if (selected != null) {
            SelectedReportCard(
                report = selected,
                onClick = { onReportClick(selected.id) },
                modifier = Modifier.align(Alignment.BottomCenter).padding(12.dp)
            )
        } else {
            Text(
                text = "${state.reports.size} reportes en el mapa",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(12.dp)
                    .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(50))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            )
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 96.dp)
        )
    }
}

/** Chips "Todos" + una por categoría; compartido por el mapa y el mapa de calor. */
@Composable
fun CategoryFilterRow(
    selected: ReportCategory?,
    onSelected: (ReportCategory?) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        item {
            FilterChip(
                selected = selected == null,
                onClick = { onSelected(null) },
                label = { Text("Todos") },
                colors = FilterChipDefaults.filterChipColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
        items(ReportCategory.entries.toList()) { category ->
            FilterChip(
                selected = selected == category,
                onClick = { onSelected(if (selected == category) null else category) },
                label = { Text(category.label) },
                colors = FilterChipDefaults.filterChipColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    }
}

@Composable
private fun SelectedReportCard(report: Report, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        modifier = modifier.fillMaxWidth().clickable(onClick = onClick)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                CategoryChip(report.category)
                StatusChip(report.status)
            }
            Text(
                text = report.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "${report.neighborhood} · Es importante: ${report.importantCount}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "Ver detalle",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}
