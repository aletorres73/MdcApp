package com.mdcapp.ui.utils

import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.unit.Dp
import java.awt.Toolkit
import java.awt.datatransfer.StringSelection

@Composable
actual fun AppBackHandler(enabled: Boolean, onBack: () -> Unit) {
    // Desktop no suele tener un botón de "atrás" de hardware. 
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
actual fun getScreenWidthDp(): Dp {
    val density = LocalDensity.current
    val windowInfo = LocalWindowInfo.current
    return with(density) { windowInfo.containerSize.width.toDp() }
}

actual fun showToast(message: String) {
    println("TOAST: $message")
}

actual fun closeApp() {
    System.exit(0)
}

actual fun shareText(text: String, title: String) {
    try {
        val selection = StringSelection(text)
        val clipboard = Toolkit.getDefaultToolkit().systemClipboard
        clipboard.setContents(selection, selection)
        showToast("Copiado al portapapeles: $title")
    } catch (e: Exception) {
        println("Error al copiar al portapapeles: ${e.message}")
    }
}

class DesktopAppInstaller : AppInstaller {
    override fun downloadAndInstall(url: String) {
        // En desktop la actualización automática suele ser distinta (Sparkle, etc.)
        // Por ahora lo dejamos vacío o abriendo el navegador.
        println("Actualización no implementada en Desktop para la URL: $url")
    }
}

actual fun getAppInstaller(context: Any?): AppInstaller {
    return DesktopAppInstaller()
}

actual val isAndroid: Boolean = false
