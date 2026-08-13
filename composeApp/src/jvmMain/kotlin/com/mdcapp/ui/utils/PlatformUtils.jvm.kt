package com.mdcapp.ui.utils

import androidx.compose.runtime.Composable

@Composable
actual fun AppBackHandler(enabled: Boolean, onBack: () -> Unit) {
    // Desktop no suele tener un botón de "atrás" de hardware. 
    // Se podría implementar el manejo de la tecla ESC si fuera necesario.
}

actual fun showToast(message: String) {
    println("TOAST: $message")
}

actual fun closeApp() {
    System.exit(0)
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
