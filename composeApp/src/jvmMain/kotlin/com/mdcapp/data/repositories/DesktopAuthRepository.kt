package com.mdcapp.data.repositories

import com.mdcapp.domain.model.AppUser
import com.mdcapp.domain.repositories.IAuthRepository
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.serialization.Serializable

class DesktopAuthRepository(private val client: HttpClient) : IAuthRepository {
    private val apiKey = "AIzaSyC8RSmswZFBr4IhOgjtTyxH0GojOtu9F8k"

    private val _authState = MutableStateFlow<AppUser?>(null)
    var idToken: String? = null
        private set

    override val currentUser: AppUser? get() = _authState.value

    override fun observeAuthState(): Flow<AppUser?> = _authState

    override suspend fun signIn(email: String, password: String): AppUser? {
        val url =
            "https://identitytoolkit.googleapis.com/v1/accounts:signInWithPassword?key=$apiKey"
        return try {
            val response = client.post(url) {
                contentType(ContentType.Application.Json)
                setBody(AuthRequest(email, password))
            }.body<AuthResponse>()

            idToken = response.idToken
            val user = AppUser(response.localId, response.email)
            _authState.value = user
            user
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    override suspend fun signUp(email: String, password: String): AppUser? {
        val url = "https://identitytoolkit.googleapis.com/v1/accounts:signUp?key=$apiKey"
        return try {
            val response = client.post(url) {
                contentType(ContentType.Application.Json)
                setBody(AuthRequest(email, password))
            }.body<AuthResponse>()

            idToken = response.idToken
            val user = AppUser(response.localId, response.email)
            _authState.value = user
            user
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    override suspend fun signOut() {
        _authState.value = null
        idToken = null
    }

    override fun isUserLoggedIn(): Boolean = _authState.value != null

    override suspend fun reauthenticate(password: String) {
        val email = currentUser?.email ?: throw Exception("Not logged in")
        signIn(email, password)
    }

    override suspend fun updatePassword(newPassword: String) {
        val url = "https://identitytoolkit.googleapis.com/v1/accounts:update?key=$apiKey"
        client.post(url) {
            contentType(ContentType.Application.Json)
            setBody(UpdatePasswordRequest(idToken ?: "", newPassword))
        }
    }
}

@Serializable
private data class AuthRequest(
    val email: String,
    val password: String,
    val returnSecureToken: Boolean = true
)

@Serializable
private data class AuthResponse(val localId: String, val email: String, val idToken: String)

@Serializable
private data class UpdatePasswordRequest(
    val idToken: String,
    val password: String,
    val returnSecureToken: Boolean = true
)
