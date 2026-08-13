package com.mdcapp.data.repositories

import com.mdcapp.domain.model.AppUser
import com.mdcapp.domain.repositories.IAuthRepository
import dev.gitlive.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class AndroidAuthRepository(private val auth: FirebaseAuth) : IAuthRepository {
    override val currentUser: AppUser?
        get() = auth.currentUser?.let { AppUser(it.uid, it.email) }

    override fun observeAuthState(): Flow<AppUser?> {
        return auth.authStateChanged.map { user ->
            user?.let { AppUser(it.uid, it.email) }
        }
    }

    override suspend fun signIn(email: String, password: String): AppUser? {
        return auth.signInWithEmailAndPassword(email, password).user?.let {
            AppUser(it.uid, it.email)
        }
    }

    override suspend fun signUp(email: String, password: String): AppUser? {
        return auth.createUserWithEmailAndPassword(email, password).user?.let {
            AppUser(it.uid, it.email)
        }
    }

    override suspend fun signOut() {
        auth.signOut()
    }

    override fun isUserLoggedIn(): Boolean {
        return auth.currentUser != null
    }

    override suspend fun reauthenticate(password: String) {
        val email = auth.currentUser?.email ?: throw Exception("User not authenticated")
        auth.signInWithEmailAndPassword(email, password)
    }

    override suspend fun updatePassword(newPassword: String) {
        auth.currentUser?.updatePassword(newPassword)
    }
}
