package com.mdcapp.ui

import androidx.compose.runtime.Composable
import com.mdcapp.domain.entities.AppRoute
import com.mdcapp.domain.repositories.AuthRepository
import com.mdcapp.ui.navigation.AppNavigation
import org.koin.compose.koinInject

@Composable
actual fun PlatformNavigation() {
    val authRepo: AuthRepository = koinInject()
    val startRoute = if (authRepo.isLogged()) AppRoute.Home.route else AppRoute.Login.route

    AppNavigation(startRoute = startRoute)
}
