package com.mdcapp.ui.utils

import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.mdcapp.OrdersApp
import com.mdcapp.domain.config.downloadInstaller
import org.koin.java.KoinJavaComponent.get

@Composable
actual fun AppBackHandler(enabled: Boolean, onBack: () -> Unit) {
    BackHandler(enabled, onBack)
}

@Composable
actual fun getScreenWidthDp(): Dp {
    return LocalConfiguration.current.screenWidthDp.dp
}

actual fun showToast(message: String) {
    try {
        val context = OrdersApp.getCurrentActivity() ?: get<Context>(Context::class.java)
        Handler(Looper.getMainLooper()).post {
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
    } catch (e: Exception) {
        // ignore
    }
}

actual fun closeApp() {
    val activity = OrdersApp.getCurrentActivity()
    if (activity != null) {
        activity.finishAffinity()
    } else {
        kotlin.system.exitProcess(0)
    }
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
    override fun downloadAndInstall(url: String, version: String) {
        downloadInstaller(context, url, version)
    }
}

actual fun getAppInstaller(context: Any?): AppInstaller {
    val ctx = context as? Context ?: get(Context::class.java)
    return AndroidAppInstaller(ctx)
}

actual val isAndroid: Boolean = true
