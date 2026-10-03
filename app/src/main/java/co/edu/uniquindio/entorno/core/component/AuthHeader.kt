package co.edu.uniquindio.entorno.core.component

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.Color

@Composable
fun AuthHeader(
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        AppLogo(size = 40)
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = "Entorno",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF111318)
        )
    }
}
