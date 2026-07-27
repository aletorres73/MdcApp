package com.mdcapp.domain.repositories

import com.mdcapp.data.service.AuthService

class AuthRepository(private val authService: AuthService) {
    suspend fun login(email: String, password: String) = authService.signIn(email, password)
    suspend fun register(email: String, password: String) = authService.signUp(email, password)
    suspend fun logout() = authService.signOut()
    fun isLogged() = authService.isUserLoggedIn()
    fun getCurrentUser() = authService.currentUser
    fun getUserId() = authService.currentUser?.uid
}

