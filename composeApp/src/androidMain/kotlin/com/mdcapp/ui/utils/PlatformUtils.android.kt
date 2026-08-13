package com.mdcapp.ui.utils

import android.content.Context
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import com.mdcapp.domain.config.downloadInstaller

@Composable
actual fun AppBackHandler(enabled: Boolean, onBack: () -> Unit) {
    BackHandler(enabled, onBack)
}

actual fun showToast(message: String) {
    // Implementación mínima para Android. Idealmente inyectar context.
}

actual fun closeApp() {
    // Necesitaría acceso a la Activity. Por ahora lo dejamos vacío o 
    // lo implementamos si tenemos una referencia.
}

class AndroidAppInstaller(private val context: Context) : AppInstaller {
    override fun downloadAndInstall(url: String) {
        downloadInstaller(context, url)
    }
}

actual fun getAppInstaller(context: Any?): AppInstaller {
    return AndroidAppInstaller(context as Context)
}
