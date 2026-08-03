package com.mdcapp.domain.service

interface AnalyticsService {
    fun logEvent(name: String, params: Map<String, Any?> = emptyMap())
    fun logScreenView(screenName: String, screenClass: String? = null)
    fun setUserIdentifier(userId: String?)
}
