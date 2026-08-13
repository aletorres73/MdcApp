package com.mdcapp.data.service

import com.mdcapp.domain.entities.RemoteInitConfig
import com.mdcapp.domain.repositories.IDatabaseRepository
import io.github.aakira.napier.Napier

class InitService(
    private val db: IDatabaseRepository
) {
    companion object {
        const val PATH = "appConfig/android/releases"
    }

    suspend fun init(): RemoteInitConfig {
        return try {
            val doc = db.getCollection(PATH, RemoteInitConfig.serializer())
                .sortedByDescending { it.versionCode }
                .firstOrNull()
                ?: RemoteInitConfig()

            Napier.i("InitService --- init: $doc")
            doc
        } catch (e: Exception) {
            Napier.e("InitService --- init error: ${e.message}", e)
            RemoteInitConfig()
        }
    }
}
