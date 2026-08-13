package com.mdcapp.data.service

import com.mdcapp.domain.service.AnalyticsService
import io.github.aakira.napier.Napier

actual class PlatformAnalyticsService : AnalyticsService {
    override fun logEvent(name: String, params: Map<String, Any?>) {
        Napier.d("Desktop Analytics - logEvent: $name, params: $params")
    }

    override fun logScreenView(screenName: String, screenClass: String?) {
        Napier.d("Desktop Analytics - logScreenView: $screenName, class: $screenClass")
    }

    override fun setUserIdentifier(userId: String?) {
        Napier.d("Desktop Analytics - setUserIdentifier: $userId")
    }
}
