package com.mdcapp.ui

import android.app.Activity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.mdcapp.domain.entities.AppRoute
import com.mdcapp.domain.repositories.AuthRepository
import com.mdcapp.ui.navigation.AppNavigation
import org.koin.compose.koinInject

@Composable
actual fun PlatformNavigation() {
    val authRepo: AuthRepository = koinInject()
    val startRoute = if (authRepo.isLogged()) AppRoute.Home.route else AppRoute.Login.route

    val view = LocalView.current
    if (!view.isInEditMode) {
        DisposableEffect(Unit) {
            val window = (view.context as Activity).window
            val insetsController = WindowCompat.getInsetsController(window, view)

            // Forzar iconos oscuros en las barras (apropiado para temas claros)
            insetsController.isAppearanceLightStatusBars = true
            insetsController.isAppearanceLightNavigationBars = true

            onDispose {}
        }
    }

    Screen {
        Box(
            modifier = Modifier
                .fillMaxSize()
        ) {
            AppNavigation(
                startRoute = startRoute
            )
        }
    }
}

