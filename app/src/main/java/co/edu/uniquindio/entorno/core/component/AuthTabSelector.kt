package co.edu.uniquindio.entorno.core.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun AuthTabSelector(
    selectedTab: AuthTab,
    onTabSelected: (AuthTab) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .clip(RoundedCornerShape(28.dp))
            .background(Color(0xFFF1F2F6))
            .padding(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Tab Iniciar sesión
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .clip(RoundedCornerShape(24.dp))
                .background(if (selectedTab == AuthTab.LOGIN) Color.White else Color.Transparent)
                .clickable { onTabSelected(AuthTab.LOGIN) },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Iniciar sesión",
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (selectedTab == AuthTab.LOGIN) Color(0xFF111318) else Color(0xFF74798D)
            )
        }

        // Tab Registrarse
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .clip(RoundedCornerShape(24.dp))
                .background(if (selectedTab == AuthTab.REGISTER) Color.White else Color.Transparent)
                .clickable { onTabSelected(AuthTab.REGISTER) },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Registrarse",
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (selectedTab == AuthTab.REGISTER) Color(0xFF111318) else Color(0xFF74798D)
            )
        }
    }
}

enum class AuthTab {
    LOGIN, REGISTER
}
