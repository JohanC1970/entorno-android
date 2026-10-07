package co.edu.uniquindio.entorno.data.remote

import android.net.Uri
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import jakarta.inject.Inject
import jakarta.inject.Singleton
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

@Singleton
class CloudinaryDataSource @Inject constructor() {

    private companion object{
        const val UPLOAD_PRESET = ""
        const val FOLDER = "entorno/reports"
    }

    suspend fun upload(localUri: String): String = suspendCancellableCoroutine { cont ->
        val requestId = MediaManager.get()
            .upload(Uri.parse(localUri))
            .unsigned(UPLOAD_PRESET)
            .option("folder", FOLDER)
            .callback(object : UploadCallback {
                override fun onStart(requestId: String) {}
                override fun onProgress(requestId: String, bytes: Long, totalBytes: Long) {}
                override fun onReschedule(requestId: String, error: ErrorInfo) {}
                override fun onSuccess(requestId: String, resultData: MutableMap<Any?, Any?>) {
                    val url = resultData["secure_url"] as? String
                    if (url != null) cont.resume(url)
                    else cont.resumeWithException(IllegalStateException("Cloudinary no devolvió URL"))
                }
                override fun onError(requestId: String, error: ErrorInfo) {
                    cont.resumeWithException(Exception(error.description))
                }
            })
            .dispatch()
        cont.invokeOnCancellation { MediaManager.get().cancelRequest(requestId) }
    }

}