package co.edu.uniquindio.entorno.core.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import co.edu.uniquindio.entorno.R

@Composable
fun AppLogo(
    modifier: Modifier = Modifier,
    size: Int = 96
) {
    Box(
        modifier = modifier
            .size(size.dp)
            .clip(RoundedCornerShape(28.dp))
            .background(Color(0xFFE2E4FF)),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = painterResource(id = R.drawable.ic_app_logo),
            contentDescription = "Logo Entorno",
            tint = Color(0xFF303CA2),
            modifier = Modifier.size((size * 0.5).dp)
        )
    }
}
