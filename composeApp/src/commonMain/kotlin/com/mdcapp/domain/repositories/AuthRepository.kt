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
    suspend fun logout() = authService.signOut()
    fun isLogged() = authService.isUserLoggedIn()
    fun getCurrentUser() = authService.currentUser
    fun getUserId() = authService.currentUser?.uid

    suspend fun getUserProfile() = userService.getUserProfile()
    suspend fun saveUserProfile(user: UserModel) = userService.saveUserProfile(user)
    suspend fun reauthenticate(password: String) = authService.reauthenticate(password)
    suspend fun updatePassword(newPassword: String) = authService.updatePassword(newPassword)
}

