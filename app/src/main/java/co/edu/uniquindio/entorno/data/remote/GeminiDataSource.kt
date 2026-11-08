package co.edu.uniquindio.entorno.data.remote

import android.content.Context
import co.edu.uniquindio.entorno.R
import dagger.hilt.android.qualifiers.ApplicationContext
import jakarta.inject.Inject
import jakarta.inject.Singleton
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
        // Revisa en Google AI Studio el nombre del modelo Flash vigente.
        const val MODEL = "gemini-3.5-flash"
        const val ENDPOINT = "https://generativelanguage.googleapis.com/v1beta/models/$MODEL:generateContent"
        const val CONNECT_TIMEOUT_MS = 10_000
        const val READ_TIMEOUT_MS = 20_000
    }

    /** Envía el prompt y devuelve el texto de la respuesta, que será JSON. */
    suspend fun generateJson(system: String, prompt: String): String = withContext(Dispatchers.IO) {
        val apiKey = context.getString(R.string.gemini_api_key)
        check(apiKey.isNotBlank()) { "Falta GEMINI_API_KEY en local.properties" }

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

        val connection = (URL(ENDPOINT).openConnection() as HttpURLConnection).apply {
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
            check(code in 200..299) { "Gemini respondió $code" }
            JSONObject(text)
                .getJSONArray("candidates").getJSONObject(0)
                .getJSONObject("content").getJSONArray("parts").getJSONObject(0)
                .getString("text")
        } finally {
            connection.disconnect()
        }
    }
}