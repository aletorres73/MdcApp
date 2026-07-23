package com.mdcapp.ui

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.mdcapp.domain.entities.AppRoute
import com.mdcapp.domain.repositories.AuthRepository
import com.mdcapp.ui.navigation.AndroidNavigation
import org.koin.compose.koinInject

@RequiresApi(Build.VERSION_CODES.O)
@Composable
actual fun PlatformNavigation() {
    val authRepo: AuthRepository = koinInject()
    val startRoute = if (authRepo.isLogged()) AppRoute.InvoicesPaged.route else AppRoute.Login.route

    Screen {
            Box(
                modifier = Modifier
                    .fillMaxSize()
            ) {
                AndroidNavigation(
                    startRoute = startRoute
                )
            }
        }
}
