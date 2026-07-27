package com.mdcapp.data.service

import android.util.Log
import com.mdcapp.domain.entities.RemoteInitConfig
import dev.gitlive.firebase.firestore.FirebaseFirestore

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

            Log.i("MDCAppOnly", "InitService --- init: $doc")
            doc
        } catch (e: Exception) {
            Log.e("MDCAppOnly", "InitService --- init: ${e.message}")
            RemoteInitConfig()
        }

    }
}
