package com.mdcapp

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.application
import com.mdcapp.di.initFirebaseApp
import com.mdcapp.di.initKoin
import com.mdcapp.ui.PlatformNavigation
import io.github.aakira.napier.DebugAntilog
import io.github.aakira.napier.Napier
import org.koin.java.KoinJavaComponent.get

fun main() {
    try {
        // Force software rendering as a first troubleshooting step for Windows crashes
        System.setProperty("skiko.renderApi", "SOFTWARE")
        println("Starting MDCapp Desktop with SOFTWARE rendering...")

        Napier.base(DebugAntilog())
        Napier.i("Napier initialized")

        initFirebaseApp(null)
        Napier.i("Firebase initialized")

        initKoin()
        Napier.i("Koin initialized")

        application {
            Window(
                onCloseRequest = ::exitApplication,
                title = "MDCapp Desktop",
                state = WindowState(
                    size = DpSize(1200.dp, 780.dp),
                    position = WindowPosition(Alignment.CenterStart)
                )
            ) {
                // Refresco por foco de ventana para Desktop
                LaunchedEffect(window.isActive) {
                    if (window.isActive) {
                        Napier.i("🪟 [Desktop] Window Focused - Refreshing data")
                        try {
                            get<com.mdcapp.domain.service.RefreshController>(com.mdcapp.domain.service.RefreshController::class.java).triggerRefresh()
                        } catch (e: Exception) {
                            Napier.e("Error triggering focus refresh", e)
                        }
                    }
                }
                PlatformNavigation()
            }
        }
    } catch (e: Throwable) {
        println("CRITICAL ERROR DURING STARTUP:")
        e.printStackTrace()
        // Ensure the error is visible even if the console closes quickly
        Thread.sleep(5000)
    }
}
