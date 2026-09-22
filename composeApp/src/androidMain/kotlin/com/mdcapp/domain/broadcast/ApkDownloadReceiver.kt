package com.mdcapp.domain.broadcast

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
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

            val downloadId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1L)
            if (downloadId == -1L) return

            val downloadManager =
                context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            val query = DownloadManager.Query().setFilterById(downloadId)
            val cursor = downloadManager.query(query)

            if (cursor != null && cursor.moveToFirst()) {
                val statusIndex = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)
                if (statusIndex != -1 && cursor.getInt(statusIndex) == DownloadManager.STATUS_SUCCESSFUL) {

                    var installUri: Uri? = downloadManager.getUriForDownloadedFile(downloadId)

                    if (installUri == null) {
                        val uriIndex = cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI)
                        if (uriIndex != -1) {
                            val fileUriString = cursor.getString(uriIndex)
                            if (!fileUriString.isNullOrEmpty()) {
                                val fileUri = Uri.parse(fileUriString)
                                val filePath = fileUri.path
                                if (filePath != null) {
                                    val file = File(filePath)
                                    if (file.exists()) {
                                        installUri = FileProvider.getUriForFile(
                                            context,
                                            "${context.packageName}.fileprovider",
                                            file
                                        )
                                    }
                                }
                            }
                        }
                    }

                    if (installUri != null) {
                        analytics.logEvent(
                            "apk_download_completed",
                            mapOf("download_id" to downloadId)
                        )

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            if (!context.packageManager.canRequestPackageInstalls()) {
                                crashlytics.log("ApkDownloadReceiver: Requesting unknown app sources permission")
                                try {
                                    val settingsIntent = Intent(
                                        Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,
                                        Uri.parse("package:${context.packageName}")
                                    ).apply {
                                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                    }
                                    context.startActivity(settingsIntent)
                                } catch (e: Exception) {
                                    crashlytics.recordException(e)
                                }
                            }
                        }

                        val installIntent = Intent(Intent.ACTION_VIEW).apply {
                            setDataAndType(installUri, "application/vnd.android.package-archive")
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK
                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        }

                        try {
                            crashlytics.log("ApkDownloadReceiver: Starting install activity")
                            context.startActivity(installIntent)
                        } catch (e: Exception) {
                            crashlytics.recordException(e)
                            analytics.logEvent(
                                "apk_install_error",
                                mapOf("error" to (e.message ?: "Unknown error"))
                            )
                        }
                    } else {
                        crashlytics.log("ApkDownloadReceiver: Unable to resolve install URI for downloadId $downloadId")
                    }
                }
                cursor.close()
            }
        }
    }
}
