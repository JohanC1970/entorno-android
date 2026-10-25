package co.edu.uniquindio.entorno.core.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import co.edu.uniquindio.entorno.core.filter.LocationScope

@Composable
fun ScopeFilterRow(
    selected: LocationScope,
    onSelected: (LocationScope) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        items(LocationScope.entries) { scope ->
            FilterChip(
                selected = selected == scope,
                onClick = { onSelected(scope) },
                label = { Text(scope.label) },
                colors = FilterChipDefaults.filterChipColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    }
}
