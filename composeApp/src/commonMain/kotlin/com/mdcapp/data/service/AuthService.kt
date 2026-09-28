package com.mdcapp.data.service

import com.mdcapp.domain.model.AppUser
import com.mdcapp.domain.repositories.IAuthRepository
import io.github.aakira.napier.Napier

class AuthService(private val auth: IAuthRepository) {

    val currentUser: AppUser?
        get() {
            val u = auth.currentUser
            println("🔑 [MDC_SESSION] AuthService.currentUser -> uid: ${u?.uid}, email: ${u?.email}")
            return u
        }

    suspend fun signIn(email: String, password: String): AppUser? {
        println("🔑 [MDC_SESSION] AuthService.signIn for $email")
        return try {
            auth.signIn(email, password)
        } catch (e: Exception) {
            println("❌ [MDC_SESSION] Error al iniciar sesión: ${e.message}")
            Napier.e("Error al iniciar sesión", e)
            null
        }
    }

    suspend fun signUp(email: String, password: String): AppUser? {
        println("🔑 [MDC_SESSION] AuthService.signUp for $email")
        return try {
            auth.signUp(email, password)
        } catch (e: Exception) {
            println("❌ [MDC_SESSION] Error al registrar usuario: ${e.message}")
            Napier.e("Error al registrar usuario", e)
            null
        }
    }

    suspend fun signOut() {
        println("🚨 [MDC_SESSION] AuthService.signOut() called")
        auth.signOut()
    }

    fun observeAuthState() = auth.observeAuthState()

    fun isUserLoggedIn(): Boolean {
        val logged = auth.isUserLoggedIn()
        println("🔑 [MDC_SESSION] AuthService.isUserLoggedIn() -> $logged")
        return logged
    }

    suspend fun reauthenticate(password: String) {
        println("🔑 [MDC_SESSION] AuthService.reauthenticate()")
        auth.reauthenticate(password)
    }

    suspend fun updatePassword(newPassword: String) {
        println("🔑 [MDC_SESSION] AuthService.updatePassword()")
        auth.updatePassword(newPassword)
    }
}

