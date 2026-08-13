package com.mdcapp.ui.utils

import androidx.compose.runtime.Composable

@Composable
expect fun AppBackHandler(enabled: Boolean = true, onBack: () -> Unit)

expect fun showToast(message: String)

expect fun closeApp()

interface AppInstaller {
    fun downloadAndInstall(url: String)
}

expect fun getAppInstaller(context: Any? = null): AppInstaller
