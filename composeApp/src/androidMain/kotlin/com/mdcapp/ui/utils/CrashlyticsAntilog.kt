package com.mdcapp.ui.utils

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.crashlytics.crashlytics
import io.github.aakira.napier.Antilog
import io.github.aakira.napier.LogLevel

class CrashlyticsAntilog : Antilog() {
    override fun performLog(
        priority: LogLevel,
        tag: String?,
        throwable: Throwable?,
        message: String?
    ) {
        if (priority < LogLevel.WARNING) return

        val crashlytics = Firebase.crashlytics

        message?.let {
            crashlytics.log("${tag ?: "LOG"}: $it")
        }

        throwable?.let {
            crashlytics.recordException(it)
        }
    }
}
