package com.mdcapp.domain.repositories

import com.mdcapp.domain.model.AppUser
import kotlinx.coroutines.flow.Flow

interface IAuthRepository {
    val currentUser: AppUser?
    fun observeAuthState(): Flow<AppUser?>
    suspend fun signIn(email: String, password: String): AppUser?
    suspend fun signUp(email: String, password: String): AppUser?
    suspend fun signOut()
    fun isUserLoggedIn(): Boolean
    suspend fun reauthenticate(password: String)
    suspend fun updatePassword(newPassword: String)
}
