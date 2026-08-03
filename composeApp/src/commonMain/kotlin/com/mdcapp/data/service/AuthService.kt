package com.mdcapp.data.service

import dev.gitlive.firebase.auth.FirebaseAuth
import dev.gitlive.firebase.auth.FirebaseUser
import io.github.aakira.napier.Napier

class AuthService(private val auth: FirebaseAuth) {

    val currentUser: FirebaseUser?
        get() = auth.currentUser

    suspend fun signIn(email: String, password: String): FirebaseUser? {
        return try {
            auth.signInWithEmailAndPassword(email, password).user
        } catch (e: Exception) {
            Napier.e("Error al iniciar sesión", e)
            null
        }
    }

    suspend fun signUp(email: String, password: String): FirebaseUser? {
        return try {
            auth.createUserWithEmailAndPassword(email, password).user
        } catch (e: Exception) {
            Napier.e("Error al registrar usuario", e)
            null
        }
    }

    suspend fun signOut() {
        auth.signOut()
    }

    fun isUserLoggedIn(): Boolean {
        return auth.currentUser != null
    }

    suspend fun reauthenticate(password: String) {
        val email = auth.currentUser?.email ?: throw Exception("Usuario no autenticado")
        auth.signInWithEmailAndPassword(email, password)
    }

    suspend fun updatePassword(newPassword: String) {
        val user = auth.currentUser ?: throw Exception("Usuario no autenticado")
        user.updatePassword(newPassword)
    }
}

