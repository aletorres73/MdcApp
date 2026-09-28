package com.mdcapp.data.service

import com.mdcapp.domain.service.ISessionManager
import com.mdcapp.domain.service.UserSession

class DesktopSessionManager : ISessionManager {
    override fun saveSession(uid: String, email: String, password: String?) {}
    override fun getSession(): UserSession? = null
    override fun clearSession() {}
    override fun isSessionValid(): Boolean = false
}
