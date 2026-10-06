package co.edu.uniquindio.entorno.core.component

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.unit.dp
import co.edu.uniquindio.entorno.core.theme.severityColor
import co.edu.uniquindio.entorno.domain.model.Severity

private fun Severity.progress(): Float = when (this) {
    Severity.LOW -> 0.33f
    Severity.MEDIUM -> 0.66f
    Severity.HIGH, Severity.CRITICAL -> 1f
}

@Composable
fun SeverityBar(
    severity: Severity,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "SEVERIDAD",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        LinearProgressIndicator(
            progress = { severity.progress() },
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 10.dp)
                .height(6.dp),
            color = severityColor(severity),
            trackColor = MaterialTheme.colorScheme.surfaceVariant,
            strokeCap = StrokeCap.Round,
            gapSize = 0.dp,
            drawStopIndicator = {}
        )
        Text(
            text = severity.label(),
            style = MaterialTheme.typography.labelMedium,
            color = severityColor(severity)
        )
    }
}
