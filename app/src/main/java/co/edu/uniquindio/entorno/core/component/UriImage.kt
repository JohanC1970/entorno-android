package co.edu.uniquindio.entorno.core.component

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private const val MAX_SIDE_PX = 1080

/**
 * Muestra una foto local (content:// o file://) decodificada fuera del hilo principal y
 * reducida para no gastar memoria. Si la URI no se puede leer (por ejemplo, una URL remota
 * o un permiso vencido), no dibuja nada.
 */
@Composable
fun UriImage(
    uri: String,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop
) {
    val context = LocalContext.current
    val bitmap by produceState<Bitmap?>(initialValue = null, uri) {
        value = withContext(Dispatchers.IO) { decodeSampled(context, uri) }
    }
    bitmap?.let {
        Image(
            bitmap = it.asImageBitmap(),
            contentDescription = null,
            contentScale = contentScale,
            modifier = modifier
        )
    }
}

private fun decodeSampled(context: android.content.Context, uri: String): Bitmap? {
    if (!uri.startsWith("content:") && !uri.startsWith("file:")) return null
    return runCatching {
        val parsed = Uri.parse(uri)
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        context.contentResolver.openInputStream(parsed)?.use { BitmapFactory.decodeStream(it, null, bounds) }
        var sample = 1
        while (bounds.outWidth / sample > MAX_SIDE_PX || bounds.outHeight / sample > MAX_SIDE_PX) sample *= 2
        val options = BitmapFactory.Options().apply { inSampleSize = sample }
        context.contentResolver.openInputStream(parsed)?.use { BitmapFactory.decodeStream(it, null, options) }
    }.getOrNull()
}
