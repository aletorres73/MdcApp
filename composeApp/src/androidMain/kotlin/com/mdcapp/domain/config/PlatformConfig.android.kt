package com.mdcapp.domain.config

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Environment
import android.util.Log
import com.mdcapp.BuildConfig
import com.mdcapp.domain.entities.RemoteInitConfig
import com.mdcapp.domain.entities.UpdateState
import com.mdcapp.domain.service.AnalyticsService
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.crashlytics.crashlytics
import org.koin.core.context.GlobalContext

actual fun checkUpdate(remote: RemoteInitConfig): Pair<UpdateState, String> {

    val currentVersion = BuildConfig.VERSION_NAME
    Log.i("MdcAppOnly", "checkUpdate: $currentVersion")

    val analytics = GlobalContext.get().get<AnalyticsService>()

    val result = if (compareVersions(currentVersion, remote.minSupported) < 0) {
        UpdateState.FORCE_UPDATE to remote.releaseNotes
    } else if (compareVersions(currentVersion, remote.versionName) < 0) {
        UpdateState.OPTIONAL_UPDATE to remote.releaseNotes
    } else {
        UpdateState.OK to ""
    }

    analytics.logEvent(
        "update_check",
        mapOf(
            "current_version" to currentVersion,
            "remote_version" to remote.versionName,
            "min_supported" to remote.minSupported,
            "result" to result.first.name,
        ),
    )

    return result
}


actual fun downloadInstaller(context: Any, url: String): Boolean {
    val crashlytics = Firebase.crashlytics
    val analytics = GlobalContext.get().get<AnalyticsService>()

    if (url.isEmpty()) {
        Log.w("MdcAppOnly", "downloadInstaller: url -> $url")
        crashlytics.log("PlatformConfig: downloadInstaller called with empty URL")
        return false
    }

    val contextAndroid = context as Context

    analytics.logEvent("update_download_started", mapOf("url" to url))
    crashlytics.log("PlatformConfig: Enqueuing download for $url")

    val request = DownloadManager.Request(Uri.parse(url)).apply {
        setTitle("Actualizando aplicación")
        setDescription("Descargando nueva versión")
        setNotificationVisibility(
            DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED,
        )

        setDestinationInExternalPublicDir(
            Environment.DIRECTORY_DOWNLOADS,
            "mdcApp.apk"
        )

        setMimeType("application/vnd.android.package-archive")
    }

    val manager = contextAndroid.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
    return try {
        manager.enqueue(request)
        true
    } catch (e: Exception) {
        crashlytics.recordException(e)
        analytics.logEvent("update_download_error", mapOf("error" to e.message))
        false
    }
}

actual val appVersion: String get() = BuildConfig.VERSION_NAME
