package com.mdcapp.domain.config

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Environment
import android.util.Log
import com.mdcapp.BuildConfig
import com.mdcapp.domain.entities.RemoteInitConfig
import com.mdcapp.domain.entities.UpdateState

actual fun checkUpdate(remote: RemoteInitConfig): Pair<UpdateState, String> {

    val currentVersion = BuildConfig.VERSION_NAME
    Log.i("MdcAppOnly", "checkUpdate: $currentVersion")

    if (compareVersions(currentVersion, remote.minSupported) < 0) {
        return UpdateState.FORCE_UPDATE to remote.releaseNotes
    }

    if (compareVersions(currentVersion, remote.versionName) < 0) {
        return UpdateState.OPTIONAL_UPDATE to remote.releaseNotes
    }

    return UpdateState.OK to ""
}


actual fun downloadInstaller(context: Any, url: String): Boolean {
    if (url.isEmpty()) {
        Log.w("MdcAppOnly", "downloadInstaller: url -> $url")
        return false
    }

    val contextAndroid = context as Context

    val request = DownloadManager.Request(Uri.parse(url)).apply {
        setTitle("Actualizando aplicación")
        setDescription("Descargando nueva versión")
        setNotificationVisibility(
            DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED
        )

        setDestinationInExternalPublicDir(
            Environment.DIRECTORY_DOWNLOADS,
            "mdcApp.apk"
        )

        setMimeType("application/vnd.android.package-archive")
    }

    val manager = contextAndroid.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
    manager.enqueue(request)
    return true
}
