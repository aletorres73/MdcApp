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
import kotlinx.coroutines.flow.catch
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

    println("🔑 [MDC_SESSION] AppNavigation composed. startRoute: $startRoute, isLogged: ${authRepo.isLogged()}, user: ${authRepo.getCurrentUser()?.uid}")

    androidx.compose.runtime.LaunchedEffect(Unit) {
        authRepo.observeAuthState().collectLatest { user ->
            val currentRoute = navController.currentDestination?.route
            println("🔑 [MDC_SESSION] AppNavigation authState emitted -> user: ${user?.uid} (${user?.email}), currentRoute: $currentRoute")
            if (user != null && (currentRoute == AppRoute.Login.route || currentRoute == null)) {
                println("🚀 [MDC_SESSION] [Navigation] Sesión detectada, redirigiendo a Home...")
                navController.navigate(AppRoute.Home.route) {
                    popUpTo(AppRoute.Login.route) { inclusive = true }
                }
            }
        }
    }

    // Lógica de redirección por suscripción en tiempo real
    androidx.compose.runtime.LaunchedEffect(Unit) {
        userService.observeUserProfile()
            .catch { println("⚠️ [MDC_SESSION] [Navigation] Error observando perfil: ${it.message}") }
            .collectLatest { profile ->
                val currentRoute = navController.currentDestination?.route
                val isLogged = authRepo.isLogged()
                println("🔑 [MDC_SESSION] AppNavigation userProfile emitted -> email: ${profile?.email}, isLogged: $isLogged, currentRoute: $currentRoute")
                if (isLogged &&
                    currentRoute != AppRoute.Login.route &&
                    currentRoute != AppRoute.SignUp.route &&
                    currentRoute != AppRoute.SubscriptionStatus.route
                ) {
                    if (profile == null) {
                        println("⚠️ [MDC_SESSION] User profile is null while logged in!")
                        return@collectLatest
                    }
                    val now = System.currentTimeMillis()
                    val isExpired = profile.subscriptionExpiresAt < now
                    val isManuallyEnabled = profile.isManuallyEnabled
                    println("🔑 [MDC_SESSION] Subscription check -> expiresAt: ${profile.subscriptionExpiresAt}, now: $now, isExpired: $isExpired, isManuallyEnabled: $isManuallyEnabled")

                    if (isExpired && !isManuallyEnabled) {
                        println("🔒 [MDC_SESSION] Redirecting to SubscriptionStatus due to expired subscription")
                        navController.navigate(AppRoute.SubscriptionStatus.route) {
                            popUpTo(AppRoute.Login.route) { inclusive = true }
                        }
                    }
                } else if (isLogged && currentRoute == AppRoute.SubscriptionStatus.route) {
                    if (profile == null) return@collectLatest
                    val now = System.currentTimeMillis()
                    val isExpired = profile.subscriptionExpiresAt < now
                    val isManuallyEnabled = profile.isManuallyEnabled
                    if (!isExpired || isManuallyEnabled) {
                        println("🔓 [MDC_SESSION] Subscription valid now, navigating back to Home")
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
