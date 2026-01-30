package com.mdcapp.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.mdcapp.domain.entities.AppRoute
import com.mdcapp.ui.navigation.AndroidNavigation

@Composable
actual fun PlatformNavigation() {
    Screen {
            Box(
                modifier = Modifier
                    .fillMaxSize()
            ) {
                AndroidNavigation(
                    startRoute = AppRoute.InvoicesPaged.route
                )
            }
        }
}