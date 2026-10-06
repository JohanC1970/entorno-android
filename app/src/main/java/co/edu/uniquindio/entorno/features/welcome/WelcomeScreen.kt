package co.edu.uniquindio.entorno.features.welcome

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import co.edu.uniquindio.entorno.core.component.AppLogo

@Composable
fun WelcomeScreen(
    onNavigateToRegister: () -> Unit,
    onNavigateToLogin: () -> Unit,
    onNavigateToTerms: () -> Unit = {},
    onNavigateToPrivacy: () -> Unit = {}
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color.White
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Spacer(modifier = Modifier.height(32.dp))

            // Logo, Título, Subtítulo, Divisor y Descripción
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                AppLogo(size = 96)

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "Entorno",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF111318)
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Tu comunidad, más segura",
                    fontSize = 16.sp,
                    color = Color(0xFF5A5F73)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Divisor sutil
                Box(
                    modifier = Modifier
                        .width(40.dp)
                        .height(2.dp)
                        .background(Color(0xFFE2E4FF), RoundedCornerShape(1.dp))
                )

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "Reporta lo que pasa en tu barrio — desde un robo hasta un hueco en la vía — y sigue en qué va cada caso.",
                    fontSize = 15.sp,
                    color = Color(0xFF5A5F73),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 16.dp),
                    lineHeight = 22.sp
                )
            }

            // Botones y enlaces inferiores
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Button(
                    onClick = onNavigateToRegister,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF303CA2)
                    )
                ) {
                    Text(
                        text = "Crear cuenta",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Enlace "¿Ya tienes cuenta? Inicia sesión"
                TextButton(onClick = onNavigateToLogin) {
                    val loginText = buildAnnotatedString {
                        append("¿Ya tienes cuenta? ")
                        withStyle(
                            style = SpanStyle(
                                color = Color(0xFF303CA2),
                                fontWeight = FontWeight.Bold
                            )
                        ) {
                            append("Inicia sesión")
                        }
                    }
                    Text(
                        text = loginText,
                        fontSize = 14.sp,
                        color = Color(0xFF5A5F73)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Texto legal de términos y privacidad interactivo
                val legalText = buildAnnotatedString {
                    append("Al registrarte, aceptas nuestros ")
                    pushStringAnnotation(tag = "terms", annotation = "terms")
                    withStyle(
                        style = SpanStyle(
                            color = Color(0xFF303CA2),
                            fontWeight = FontWeight.SemiBold,
                            textDecoration = TextDecoration.Underline
                        )
                    ) {
                        append("Términos")
                    }
                    pop()
                    append(" y la ")
                    pushStringAnnotation(tag = "privacy", annotation = "privacy")
                    withStyle(
                        style = SpanStyle(
                            color = Color(0xFF303CA2),
                            fontWeight = FontWeight.SemiBold,
                            textDecoration = TextDecoration.Underline
                        )
                    ) {
                        append("Política de privacidad")
                    }
                    pop()
                    append(".")
                }

                ClickableText(
                    text = legalText,
                    style = LocalTextStyle.current.copy(
                        fontSize = 12.sp,
                        color = Color(0xFF74798D),
                        textAlign = TextAlign.Center
                    ),
                    modifier = Modifier.padding(horizontal = 8.dp),
                    onClick = { offset ->
                        legalText.getStringAnnotations(tag = "terms", start = offset, end = offset)
                            .firstOrNull()?.let { onNavigateToTerms() }
                        legalText.getStringAnnotations(tag = "privacy", start = offset, end = offset)
                            .firstOrNull()?.let { onNavigateToPrivacy() }
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true, widthDp = 392, heightDp = 800)
@Composable
fun WelcomeScreenPreview() {
    WelcomeScreen(
        onNavigateToRegister = {},
        onNavigateToLogin = {}
    )
}
