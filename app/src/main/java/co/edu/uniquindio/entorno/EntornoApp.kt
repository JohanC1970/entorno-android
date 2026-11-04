package co.edu.uniquindio.entorno

import android.app.Application
import com.cloudinary.android.MediaManager
import dagger.hilt.android.HiltAndroidApp
import co.edu.uniquindio.entorno.R

@HiltAndroidApp
class EntornoApp : Application() {
    override fun onCreate() {
        super.onCreate()
        MediaManager.init(
            this,
            mapOf(
                "cloud_name" to getString(R.string.cloudinary_cloud_name),
                "secure" to true
            )
        )
    }
}
