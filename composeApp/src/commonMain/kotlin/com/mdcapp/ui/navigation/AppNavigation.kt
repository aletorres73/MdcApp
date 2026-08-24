package com.mdcapp.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.mdcapp.domain.entities.AppRoute
import com.mdcapp.ui.screens.LoginScreen
import com.mdcapp.ui.screens.MainScreen
import com.mdcapp.ui.screens.SignUpScreen
import com.mdcapp.ui.screens.SubscriptionStatusScreen
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import org.koin.core.annotation.KoinExperimentalAPI

@OptIn(KoinExperimentalAPI::class)
@Composable
fun AppNavigation(startRoute: String) {
    val navController = rememberNavController()
    val authRepo: com.mdcapp.domain.repositories.AuthRepository = koinInject()
    val userService: com.mdcapp.data.service.UserService = koinInject()
    val scope = androidx.compose.runtime.rememberCoroutineScope()

    androidx.compose.runtime.LaunchedEffect(Unit) {
        authRepo.observeAuthState().collectLatest { user ->
            val currentRoute = navController.currentDestination?.route
            if (user != null && (currentRoute == AppRoute.Login.route || currentRoute == null)) {
                println("🚀 [Navigation] Sesión detectada, redirigiendo a Home...")
                navController.navigate(AppRoute.Home.route) {
                    popUpTo(AppRoute.Login.route) { inclusive = true }
                }
            }
        }
    }

    // Lógica de redirección por suscripción en tiempo real
    androidx.compose.runtime.LaunchedEffect(Unit) {
        userService.observeUserProfile().collectLatest { profile ->
            val currentRoute = navController.currentDestination?.route
            if (authRepo.isLogged() &&
                currentRoute != AppRoute.Login.route &&
                currentRoute != AppRoute.SignUp.route &&
                currentRoute != AppRoute.SubscriptionStatus.route
            ) {
                val isExpired = (profile?.subscriptionExpiresAt ?: 0) < System.currentTimeMillis()
                val isManuallyEnabled = profile?.isManuallyEnabled == true

                if (isExpired && !isManuallyEnabled) {
                    navController.navigate(AppRoute.SubscriptionStatus.route) {
                        popUpTo(AppRoute.Login.route) { inclusive = true }
                    }
                }
            } else if (authRepo.isLogged() && currentRoute == AppRoute.SubscriptionStatus.route) {
                // Si estamos en la pantalla de bloqueo y la suscripción se activa, salir de ahí
                val isExpired = (profile?.subscriptionExpiresAt ?: 0) < System.currentTimeMillis()
                val isManuallyEnabled = profile?.isManuallyEnabled == true
                if (!isExpired || isManuallyEnabled) {
                    navController.navigate(AppRoute.Home.route) {
                        popUpTo(AppRoute.SubscriptionStatus.route) { inclusive = true }
                    }
                }
            }
        }
    }

    NavHost(
        navController = navController,
        startDestination = startRoute
    ) {
        composable(route = AppRoute.Login.route) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(AppRoute.Home.route) {
                        popUpTo(AppRoute.Login.route) { inclusive = true }
                    }
                },
                onRegisterClick = {
                    navController.navigate(AppRoute.SignUp.route)
                }
            )
        }

        composable(route = AppRoute.SignUp.route) {
            SignUpScreen(
                onBack = { navController.popBackStack() },
                onSignUpSuccess = {
                    navController.navigate(AppRoute.Home.route) {
                        popUpTo(AppRoute.SignUp.route) { inclusive = true }
                    }
                }
            )
        }

        composable(route = AppRoute.Home.route) {
            MainScreen(
                onLogout = {
                    scope.launch {
                        authRepo.logout()
                        navController.navigate(AppRoute.Login.route) {
                            popUpTo(AppRoute.Home.route) { inclusive = true }
                        }
                    }
                }
            )
        }

        composable(route = AppRoute.SubscriptionStatus.route) {
            SubscriptionStatusScreen(
                onLogout = {
                    scope.launch {
                        authRepo.logout()
                        navController.navigate(AppRoute.Login.route) {
                            popUpTo(AppRoute.Login.route) { inclusive = true }
                        }
                    }
                }
            )
        }
    }
}
