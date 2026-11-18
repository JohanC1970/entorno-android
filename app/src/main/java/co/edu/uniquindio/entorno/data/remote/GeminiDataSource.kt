package co.edu.uniquindio.entorno.data.remote

import android.content.Context
import co.edu.uniquindio.entorno.R
import dagger.hilt.android.qualifiers.ApplicationContext
import jakarta.inject.Inject
import jakarta.inject.Singleton
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

@Singleton
class GeminiDataSource @Inject constructor(
    @param:ApplicationContext private val context: Context
) {
    private companion object {
        // Se prueban en orden: si el primero está saturado (429/5xx) o no responde, se usa el siguiente.
        // Revisa en Google AI Studio los nombres de modelo vigentes.
        val MODELS = listOf("gemini-3.5-flash-lite", "gemini-3.1-flash-lite")
        const val ENDPOINT_BASE = "https://generativelanguage.googleapis.com/v1beta/models"
        // Cortos a propósito: la IA corre antes de publicar y no debe hacer esperar al usuario.
        const val CONNECT_TIMEOUT_MS = 4_000
        const val READ_TIMEOUT_MS = 6_000
    }

    /** Envía el prompt y devuelve el texto de la respuesta, que será JSON. */
    suspend fun generateJson(system: String, prompt: String): String = withContext(Dispatchers.IO) {
        val apiKey = context.getString(R.string.gemini_api_key)
        check(apiKey.isNotBlank()) { "Falta GEMINI_API_KEY en local.properties" }
        var lastError: Exception = IllegalStateException("Gemini no respondió")
        for (model in MODELS) {
            try {
                return@withContext requestModel(model, apiKey, system, prompt)
            } catch (e: RetryableException) {
                lastError = e
            } catch (e: IOException) {
                lastError = e
            }
        }
        throw lastError
    }

    /** Errores que justifican probar con otro modelo (saturación o límite de uso). */
    private class RetryableException(message: String) : Exception(message)

    private fun requestModel(model: String, apiKey: String, system: String, prompt: String): String {

        val body = JSONObject()
            .put("systemInstruction", JSONObject().put("parts", JSONArray().put(JSONObject().put("text", system))))
            .put(
                "contents",
                JSONArray().put(
                    JSONObject().put("role", "user")
                        .put("parts", JSONArray().put(JSONObject().put("text", prompt)))
                )
            )
            .put(
                "generationConfig",
                JSONObject().put("responseMimeType", "application/json").put("temperature", 0)
            )

        val connection = (URL("$ENDPOINT_BASE/$model:generateContent").openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            connectTimeout = CONNECT_TIMEOUT_MS
            readTimeout = READ_TIMEOUT_MS
            doOutput = true
            setRequestProperty("Content-Type", "application/json")
            setRequestProperty("x-goog-api-key", apiKey)
        }
        try {
            connection.outputStream.use { it.write(body.toString().toByteArray()) }
            val code = connection.responseCode
            val stream = if (code in 200..299) connection.inputStream else connection.errorStream
            val text = stream?.bufferedReader()?.use { it.readText() }.orEmpty()
            if (code == 429 || code >= 500) throw RetryableException("Gemini ($model) respondió $code")
            check(code in 200..299) { "Gemini ($model) respondió $code" }
            return JSONObject(text)
                .getJSONArray("candidates").getJSONObject(0)
                .getJSONObject("content").getJSONArray("parts").getJSONObject(0)
                .getString("text")
        } finally {
            connection.disconnect()
        }
    }
}