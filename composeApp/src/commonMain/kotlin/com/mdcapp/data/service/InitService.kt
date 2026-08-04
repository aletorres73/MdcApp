package com.mdcapp.data.service

import com.mdcapp.domain.entities.RemoteInitConfig
import dev.gitlive.firebase.firestore.Direction
import dev.gitlive.firebase.firestore.FirebaseFirestore
import io.github.aakira.napier.Napier

class InitService(
    private val db: FirebaseFirestore
) {
    companion object {
        const val PATH = "appConfig/android/releases"
    }

    suspend fun init(): RemoteInitConfig {
        return try {
            val doc = db
                .collection(PATH)
                .orderBy("versionCode", Direction.DESCENDING)
                .limit(1)
                .get()
                .documents.firstOrNull()?.data<RemoteInitConfig>()
                ?: RemoteInitConfig()

            Napier.i("InitService --- init: $doc")
            doc
        } catch (e: Exception) {
            Napier.e("InitService --- init error: ${e.message}", e)
            RemoteInitConfig()
        }
    }
}
