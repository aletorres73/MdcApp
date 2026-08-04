package com.mdcapp.domain.broadcast

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Environment
import androidx.core.content.FileProvider
import com.mdcapp.domain.service.AnalyticsService
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.crashlytics.crashlytics
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.io.File

class ApkDownloadReceiver : BroadcastReceiver(), KoinComponent {

    private val analytics: AnalyticsService by inject()

    override fun onReceive(context: Context, intent: Intent) {

        if (intent.action == DownloadManager.ACTION_DOWNLOAD_COMPLETE) {
            val crashlytics = Firebase.crashlytics
            crashlytics.log("ApkDownloadReceiver: Download complete action received")

            val file = File(
                Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_DOWNLOADS
                ),
                "mdcApp.apk"
            )

            if (!file.exists()) {
                crashlytics.log("ApkDownloadReceiver: File mdcApp.apk not found in Downloads")
                return
            }

            analytics.logEvent("apk_download_completed", mapOf("file_size" to file.length()))

            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )

            val installIntent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/vnd.android.package-archive")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            try {
                crashlytics.log("ApkDownloadReceiver: Starting install activity")
                context.startActivity(installIntent)
            } catch (e: Exception) {
                crashlytics.recordException(e)
                analytics.logEvent("apk_install_error", mapOf("error" to e.message))
            }
        }
    }
}
