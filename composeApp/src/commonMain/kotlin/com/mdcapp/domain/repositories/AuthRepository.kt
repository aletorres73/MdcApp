package com.mdcapp.domain.repositories

import com.mdcapp.data.service.AuthService
import com.mdcapp.data.service.UserService
import com.mdcapp.domain.entities.UserModel

class AuthRepository(
    private val authService: AuthService,
    private val userService: UserService
) {
    suspend fun login(email: String, password: String) = authService.signIn(email, password)
    suspend fun register(email: String, password: String) = authService.signUp(email, password)
    suspend fun logout() {
        println("🚨 [MDC_SESSION] AuthRepository.logout() requested")
        authService.signOut()
    }
    fun observeAuthState() = authService.observeAuthState()
    fun isLogged(): Boolean {
        val logged = authService.isUserLoggedIn()
        println("🔑 [MDC_SESSION] AuthRepository.isLogged() -> $logged")
        return logged
    }
    fun getCurrentUser() = authService.currentUser
    fun getUserId() = authService.currentUser?.uid

    suspend fun getUserProfile() = userService.getUserProfile()
    suspend fun saveUserProfile(user: UserModel) = userService.saveUserProfile(user)
    suspend fun reauthenticate(password: String) = authService.reauthenticate(password)
    suspend fun updatePassword(newPassword: String) = authService.updatePassword(newPassword)
}

