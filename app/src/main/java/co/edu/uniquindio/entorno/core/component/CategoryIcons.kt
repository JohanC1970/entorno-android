package co.edu.uniquindio.entorno.core.component

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.ui.graphics.vector.ImageVector
import co.edu.uniquindio.entorno.domain.model.ReportCategory

// TODO: reemplazar por iconos propios de cada categoría (escudo, cruz médica,
// triángulo de obra, huella, grupo de personas) como en los mockups de Fase 1
fun ReportCategory.icon(): ImageVector = when (this) {
    ReportCategory.SECURITY -> Icons.Default.Lock
    ReportCategory.MEDICAL -> Icons.Default.Favorite
    ReportCategory.INFRASTRUCTURE -> Icons.Default.Build
    ReportCategory.PETS -> Icons.Default.Face
    ReportCategory.COMMUNITY -> Icons.Default.Person
}
