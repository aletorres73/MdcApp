package com.mdcapp.data.service

import com.mdcapp.domain.model.AppUser
import com.mdcapp.domain.repositories.IAuthRepository
import io.github.aakira.napier.Napier

class AuthService(private val auth: IAuthRepository) {

    val currentUser: AppUser?
        get() = auth.currentUser

    suspend fun signIn(email: String, password: String): AppUser? {
        return try {
            auth.signIn(email, password)
        } catch (e: Exception) {
            Napier.e("Error al iniciar sesión", e)
            null
        }
    }

    suspend fun signUp(email: String, password: String): AppUser? {
        return try {
            auth.signUp(email, password)
        } catch (e: Exception) {
            Napier.e("Error al registrar usuario", e)
            null
        }
    }

    suspend fun signOut() {
        auth.signOut()
    }

    fun observeAuthState() = auth.observeAuthState()

    fun isUserLoggedIn(): Boolean {
        return auth.isUserLoggedIn()
    }

    suspend fun reauthenticate(password: String) {
        auth.reauthenticate(password)
    }

    suspend fun updatePassword(newPassword: String) {
        auth.updatePassword(newPassword)
    }
}

