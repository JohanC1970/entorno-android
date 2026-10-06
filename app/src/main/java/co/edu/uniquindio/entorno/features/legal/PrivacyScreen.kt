package co.edu.uniquindio.entorno.features.legal

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrivacyScreen(
    onBackClick: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Política de Privacidad",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF111318)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver",
                            tint = Color(0xFF111318)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        containerColor = Color.White
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(24.dp)
        ) {
            Text(
                text = "Política de Privacidad de Entorno",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF111318)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Última actualización: " + java.time.LocalDate.now(),
                fontSize = 13.sp,
                color = Color(0xFF74798D)
            )

            Spacer(modifier = Modifier.height(20.dp))

            SectionTitle("1. Información que Recopilamos")
            SectionText("Para prestar nuestros servicios de seguridad comunitaria, recopilamos tu nombre, correo electrónico, ciudad, dirección y coordenadas de geolocalización cuando registras reportes en tu área.")

            SectionTitle("2. Uso de la Información")
            SectionText("Tus datos se utilizan exclusivamente para validar tus publicaciones, mostrar reportes relevantes en tu vecindario mediante mapas interactivos y enviar notificaciones importantes sobre el estado de tus incidentes.")

            SectionTitle("3. Almacenamiento Seguro")
            SectionText("Toda tu información está protegida mediante los protocolos de seguridad de Firebase Cloud Firestore y la autenticación cifrada de Firebase Auth.")

            SectionTitle("4. Compartir Información")
            SectionText("No vendemos ni comercializamos tus datos personales con terceros. Los reportes comunitarios son públicos de forma anónima o con el nombre del usuario para el bien común del vecindario.")

            SectionTitle("5. Tus Derechos")
            SectionText("Puedes actualizar o eliminar tus datos en cualquier momento accediendo a la configuración de tu perfil o solicitando la eliminación de tu cuenta.")

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun SectionTitle(title: String) {
    Text(
        text = title,
        fontSize = 16.sp,
        fontWeight = FontWeight.SemiBold,
        color = Color(0xFF303CA2),
        modifier = Modifier.padding(top = 16.dp, bottom = 6.dp)
    )
}

@Composable
private fun SectionText(text: String) {
    Text(
        text = text,
        fontSize = 14.sp,
        color = Color(0xFF5A5F73),
        lineHeight = 20.sp
    )
}
