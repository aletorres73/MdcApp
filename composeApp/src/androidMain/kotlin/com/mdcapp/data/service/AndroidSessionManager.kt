package com.mdcapp.data.service

import android.content.Context
import android.content.SharedPreferences
import com.mdcapp.domain.service.ISessionManager
import com.mdcapp.domain.service.UserSession

class AndroidSessionManager(context: Context) : ISessionManager {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("mdc_user_session_v1", Context.MODE_PRIVATE)

    override fun saveSession(uid: String, email: String, password: String?) {
        println("🔑 [MDC_SESSION] AndroidSessionManager.saveSession() -> uid: $uid, email: $email, hasPassword: ${!password.isNullOrEmpty()}")
        val editor = prefs.edit()
            .putString("key_uid", uid)
            .putString("key_email", email)
            .putLong("key_timestamp", System.currentTimeMillis())

        if (!password.isNullOrEmpty()) {
            editor.putString("key_password", password)
        }
        editor.commit() // Synchronous disk commit
    }

    override fun getSession(): UserSession? {
        val uid = prefs.getString("key_uid", null) ?: return null
        if (uid.isEmpty()) return null
        val email = prefs.getString("key_email", "") ?: ""
        val password = prefs.getString("key_password", null)
        val timestamp = prefs.getLong("key_timestamp", 0L)
        println("🔑 [MDC_SESSION] AndroidSessionManager.getSession() -> uid: $uid, email: $email, hasPassword: ${!password.isNullOrEmpty()}")
        return UserSession(
            uid = uid,
            email = email,
            password = password,
            loginTimestamp = timestamp
        )
    }

    override fun clearSession() {
        println("🔑 [MDC_SESSION] AndroidSessionManager.clearSession() requested")
        prefs.edit().clear().commit() // Synchronous disk commit
    }

    override fun isSessionValid(): Boolean {
        val session = getSession() ?: return false
        val valid = session.uid.isNotEmpty()
        println("🔑 [MDC_SESSION] AndroidSessionManager.isSessionValid() -> $valid (uid: ${session.uid})")
        return valid
    }
}
