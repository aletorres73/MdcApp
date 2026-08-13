package com.mdcapp.domain.config

import com.mdcapp.domain.entities.RemoteInitConfig
import com.mdcapp.domain.entities.UpdateState

actual fun checkUpdate(remote: RemoteInitConfig): Pair<UpdateState, String> {
    return UpdateState.OK to ""
}

actual fun downloadInstaller(context: Any, url: String): Boolean {
    return false
}

actual val appVersion: String get() = "1.0.0"
