package com.mdcapp.data.service

import com.mdcapp.domain.entities.RemoteInitConfig
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
                .get()
                .documents.map { it.data<RemoteInitConfig>() }
                .last()

            Napier.i("InitService --- init: $doc")
            doc
        } catch (e: Exception) {
            Napier.e("InitService --- init: ${e.message}", e)
            RemoteInitConfig()
        }

    }
}
