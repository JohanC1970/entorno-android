package co.edu.uniquindio.entorno.features.home

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import co.edu.uniquindio.entorno.core.component.ReportCard
import co.edu.uniquindio.entorno.core.component.icon
import co.edu.uniquindio.entorno.core.theme.EntornoTheme
import co.edu.uniquindio.entorno.core.theme.categoryColors
import co.edu.uniquindio.entorno.domain.model.Location
import co.edu.uniquindio.entorno.domain.model.Report
import co.edu.uniquindio.entorno.domain.model.ReportCategory
import co.edu.uniquindio.entorno.domain.model.ReportStatus
import java.time.LocalDate

private data class BottomDestination(val label: String, val icon: ImageVector)

private val bottomDestinations = listOf(
    BottomDestination("Inicio", Icons.Default.Home),
    BottomDestination("Mapa", Icons.Default.Place),
    BottomDestination("Mis reportes", Icons.AutoMirrored.Filled.List),
    BottomDestination("Perfil", Icons.Default.Person)
)

@Composable
fun HomeScreen(
    userName: String,
    reports: List<Report>,
    onReportClick: (Report) -> Unit = {},
    onCreateClick: () -> Unit = {},
    onNotificationsClick: () -> Unit = {},
    onMapClick: () -> Unit = {}
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        floatingActionButton = {
            FloatingActionButton(
                onClick = onCreateClick,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Crear reporte")
            }
        },
        bottomBar = {
            NavigationBar {
                bottomDestinations.forEachIndexed { index, destination ->
                    NavigationBarItem(
                        selected = index == 0,
                        onClick = if (destination.label == "Mapa") onMapClick else ({}),
                        icon = { Icon(destination.icon, contentDescription = null) },
                        label = { Text(destination.label) }
                    )
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item { Greeting(userName, onNotificationsClick) }
            item { SearchBarPlaceholder() }
            item { CategoryFilters() }
            item { SectionHeader(onMapClick) }
            items(reports, key = { it.id }) { report ->
                ReportCard(report = report, onClick = { onReportClick(report) })
            }
        }
    }
}

private fun initials(name: String): String =
    name.split(" ")
        .filter { it.isNotBlank() }
        .take(2)
        .joinToString("") { it.first().uppercase() }

@Composable
private fun Greeting(userName: String, onNotificationsClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = initials(userName),
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
        Spacer(Modifier.width(12.dp))
        androidx.compose.foundation.layout.Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Buenos días",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = userName,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
        }
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .clickable(onClick = onNotificationsClick),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Notifications,
                contentDescription = "Notificaciones",
                tint = MaterialTheme.colorScheme.onBackground
            )
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 10.dp, end = 10.dp)
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.error)
            )
        }
    }
}

@Composable
private fun SearchBarPlaceholder() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(50),
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.width(12.dp))
            Text(
                text = "Buscar en tu zona",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun CategoryFilters() {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        item {
            FilterChip(
                selected = true,
                onClick = {},
                label = { Text("Todos") },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
        items(ReportCategory.entries) { category ->
            val colors = categoryColors(category)
            FilterChip(
                selected = false,
                onClick = {},
                label = { Text(category.label) },
                leadingIcon = {
                    Icon(
                        imageVector = category.icon(),
                        contentDescription = null,
                        modifier = Modifier.size(FilterChipDefaults.IconSize)
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    containerColor = colors.chipBackground,
                    labelColor = colors.chipText,
                    iconColor = colors.icon
                ),
                border = null
            )
        }
    }
}

@Composable
private fun SectionHeader(onMapClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Reportes cerca de ti",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = "Ver mapa",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.clickable(onClick = onMapClick)
        )
    }
}

/** Datos temporales para previews y pruebas visuales. */
object SampleReports {
    val reports: List<Report> = listOf(
        Report(
            id = "1",
            title = "Hueco grande en la vía principal frente al colegio",
            description = "",
            category = ReportCategory.INFRASTRUCTURE,
            location = Location(4.5339, -75.6811),
            status = ReportStatus.VERIFIED,
            imageUrls = emptyList(),
            ownerId = "u1",
            date = LocalDate.of(2026, 2, 18),
            importantCount = 20,
            commentCount = 6
        ),
        Report(
            id = "2",
            title = "Robo reiterado de celulares en el parque principal durante las noches",
            description = "",
            category = ReportCategory.SECURITY,
            location = Location(4.5350, -75.6750),
            status = ReportStatus.PENDING,
            imageUrls = emptyList(),
            ownerId = "u2",
            date = LocalDate.of(2026, 2, 17),
            importantCount = 35,
            commentCount = 14
        ),
        Report(
            id = "3",
            title = "Perro perdido con collar azul",
            description = "",
            category = ReportCategory.PETS,
            location = Location(4.5300, -75.6900),
            status = ReportStatus.RESOLVED,
            imageUrls = emptyList(),
            ownerId = "u3",
            date = LocalDate.of(2026, 2, 15),
            importantCount = 8,
            commentCount = 3
        ),
        Report(
            id = "4",
            title = "Jornada de limpieza comunitaria del sábado",
            description = "",
            category = ReportCategory.COMMUNITY,
            location = Location(4.5400, -75.6700),
            status = ReportStatus.REJECTED,
            imageUrls = emptyList(),
            ownerId = "u4",
            date = LocalDate.of(2026, 2, 12),
            importantCount = 2,
            commentCount = 0
        )
    )
}

@Preview(showBackground = true)
@Composable
private fun HomeScreenPreview() {
    EntornoTheme {
        HomeScreen(userName = "Juan Cayón", reports = SampleReports.reports)
    }
}

@Preview(showBackground = true, uiMode = UI_MODE_NIGHT_YES)
@Composable
private fun HomeScreenDarkPreview() {
    EntornoTheme {
        HomeScreen(userName = "Juan Cayón", reports = SampleReports.reports)
    }
}
