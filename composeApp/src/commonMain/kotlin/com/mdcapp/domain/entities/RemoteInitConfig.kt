package com.mdcapp.domain.entities

import kotlinx.serialization.Serializable

@Serializable
data class RemoteInitConfig(
    val apkUrl: String = "",
    val enable: Boolean = true,
    val minSupported: String = "",
    val releaseNotes: String = "",
    val versionCode: Int = 0,
    val versionName: String = ""
)
