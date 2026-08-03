package com.mdcapp.data.service

import com.mdcapp.domain.service.AnalyticsService
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.analytics.analytics

class FirebaseAnalyticsService : AnalyticsService {

    private val analytics by lazy { Firebase.analytics }

    override fun logEvent(name: String, params: Map<String, Any?>) {
        try {
            // Convertir Map<String, Any?> a Map<String, Any> filtrando nulos si es necesario
            val nonNullParams = params.filterValues { it != null }.mapValues { it.value!! }
            analytics.logEvent(name, nonNullParams)
        } catch (e: Exception) {
            // Silencioso
        }
    }

    override fun logScreenView(screenName: String, screenClass: String?) {
        try {
            val params = mutableMapOf<String, Any>("screen_name" to screenName)
            screenClass?.let { params["screen_class"] = it }
            analytics.logEvent("screen_view", params)
        } catch (e: Exception) {
            // Silencioso
        }
    }

    override fun setUserIdentifier(userId: String?) {
        try {
            analytics.setUserId(userId)
        } catch (e: Exception) {
            // Silencioso
        }
    }
}
