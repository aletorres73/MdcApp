package com.mdcapp.domain.service

data class UserSession(
    val uid: String,
    val email: String,
    val password: String? = null,
    val loginTimestamp: Long = 0L
)

interface ISessionManager {
    fun saveSession(uid: String, email: String, password: String? = null)
    fun getSession(): UserSession?
    fun clearSession()
    fun isSessionValid(): Boolean
}
