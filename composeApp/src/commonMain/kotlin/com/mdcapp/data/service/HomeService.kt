@file:Suppress("INLINE_FROM_HIGHER_PLATFORM")

package com.mdcapp.data.service

import com.mdcapp.data.remote.RemoteResultFactoryModel
import dev.gitlive.firebase.firestore.FirebaseFirestore

class HomeService(
    private val db: FirebaseFirestore,
    private val authService: AuthService
) {
    companion object {
        const val FACTORIES = "factories"
    }

    private val userId: String
        get() = authService.currentUser?.uid ?: "unknown"

    private val factoriesCollection
        get() = db.collection("users").document(userId).collection(FACTORIES)

    suspend fun fetchAllFactories(): List<RemoteResultFactoryModel> {
        return try {
            val documents = factoriesCollection
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
