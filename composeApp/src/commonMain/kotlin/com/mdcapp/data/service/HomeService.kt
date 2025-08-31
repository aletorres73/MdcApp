@file:Suppress("INLINE_FROM_HIGHER_PLATFORM")

package com.mdcapp.data.service

import com.mdcapp.data.remote.RemoteResultFactoryModel
import dev.gitlive.firebase.firestore.FirebaseFirestore

class HomeService(private val db: FirebaseFirestore) {
    companion object {
        const val FACTORIES = "factories"
    }

    suspend fun fetchAllFactories(): List<RemoteResultFactoryModel> {
        return try {
            val documents = db.collection(FACTORIES)
                .get()
                .documents
                .map { it.data<RemoteResultFactoryModel>() }
            println("on fetchAllFactories in firestore: $documents")
            documents
        } catch (e: Exception) {
            println("Firestore : on firestore getFactories: $e")
            emptyList()
        }
    }

}