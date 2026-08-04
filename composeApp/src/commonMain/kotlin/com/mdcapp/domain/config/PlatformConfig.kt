package com.mdcapp.domain.config

import com.mdcapp.domain.entities.RemoteInitConfig
import com.mdcapp.domain.entities.UpdateState


expect fun checkUpdate(remote: RemoteInitConfig): Pair<UpdateState, String>

expect fun downloadInstaller(context: Any, url: String): Boolean

expect val appVersion: String

fun compareVersions(v1: String, v2: String): Int {
    val parts1 = v1.split(".").map { it.toIntOrNull() ?: 0 }
    val parts2 = v2.split(".").map { it.toIntOrNull() ?: 0 }

    val maxLength = maxOf(parts1.size, parts2.size)

    for (i in 0 until maxLength) {
        val p1 = parts1.getOrElse(i) { 0 }
        val p2 = parts2.getOrElse(i) { 0 }

        if (p1 != p2) return p1.compareTo(p2)
    }

    return 0
}
