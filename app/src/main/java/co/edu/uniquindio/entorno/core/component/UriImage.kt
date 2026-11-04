package co.edu.uniquindio.entorno.core.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import coil3.compose.AsyncImage

/** Foto local (content://, file://) o remota (https://), cargada y reducida por Coil. */
@Composable
fun UriImage(
    uri: String,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop
) {
    AsyncImage(
        model = uri,
        contentDescription = null,
        contentScale = contentScale,
        modifier = modifier
    )
}