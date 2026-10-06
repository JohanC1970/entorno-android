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
fun TermsScreen(
    onBackClick: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Términos y Condiciones",
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
                text = "Términos de Servicio de Entorno",
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

            SectionTitle("1. Aceptación de los Términos")
            SectionText("Al registrarte o utilizar la plataforma Entorno, aceptas cumplir con los presentes Términos de Servicio. Si no estás de acuerdo con alguna de las cláusulas, no debes acceder a la aplicación.")

            SectionTitle("2. Uso de la Comunidad")
            SectionText("Entorno es una plataforma colaborativa para reportar incidencias comunitarias y de seguridad. Te comprometes a publicar únicamente información verídica, respetuosa y constructiva sobre eventos en tu vecindario.")

            SectionTitle("3. Contenido Prohibido")
            SectionText("Queda estrictamente prohibido la publicación de información falsa, difamatoria, lenguaje de odio, contenido acosador o material engañoso. La infracción de esta norma puede acarrear la suspensión definitiva de tu cuenta por parte de los moderadores.")

            SectionTitle("4. Moderación y Gamificación")
            SectionText("Los moderadores autorizados revisarán los reportes y asignarán insignias o puntos según la veracidad y utilidad de la información suministrada.")

            SectionTitle("5. Modificaciones")
            SectionText("Nos reservamos el derecho de actualizar estos términos en cualquier momento. Te notificaremos sobre cambios significativos a través de la aplicación.")

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
