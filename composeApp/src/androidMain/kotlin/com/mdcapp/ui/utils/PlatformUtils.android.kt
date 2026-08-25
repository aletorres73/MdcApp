package com.mdcapp.ui.utils

import android.content.Context
import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import com.mdcapp.domain.config.downloadInstaller
import org.koin.java.KoinJavaComponent.get

@Composable
actual fun AppBackHandler(enabled: Boolean, onBack: () -> Unit) {
    BackHandler(enabled, onBack)
}

actual fun showToast(message: String) {
    // Implementación mínima para Android.
}

actual fun closeApp() {
    // Implementación mínima.
}

actual fun shareText(text: String, title: String) {
    try {
        val context = get<Context>(Context::class.java)
        val sendIntent: Intent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, text)
            type = "text/plain"
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        val shareIntent = Intent.createChooser(sendIntent, title)
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(shareIntent)
    } catch (e: Exception) {
        // Log error
    }
}

class AndroidAppInstaller(private val context: Context) : AppInstaller {
    override fun downloadAndInstall(url: String) {
        downloadInstaller(context, url)
    }
}

actual fun getAppInstaller(context: Any?): AppInstaller {
    return AndroidAppInstaller(context as Context)
}
