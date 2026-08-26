package com.mdcapp.ui.utils

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.Dp

@Composable
expect fun AppBackHandler(enabled: Boolean = true, onBack: () -> Unit)

@Composable
expect fun getScreenWidthDp(): Dp

expect fun showToast(message: String)

expect fun closeApp()

expect fun shareText(text: String, title: String = "Compartir")

interface AppInstaller {
    fun downloadAndInstall(url: String)
}

expect fun getAppInstaller(context: Any? = null): AppInstaller

expect val isAndroid: Boolean
