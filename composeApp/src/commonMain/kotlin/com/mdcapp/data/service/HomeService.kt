@file:Suppress("INLINE_FROM_HIGHER_PLATFORM")

package com.mdcapp.data.service

import com.mdcapp.data.remote.RemoteResultFactoryModel
import com.mdcapp.domain.repositories.IDatabaseRepository

class HomeService(
    private val db: IDatabaseRepository,
    private val authService: AuthService
) {
    companion object {
        const val FACTORIES = "factories"
    }

    private val userId: String
        get() = authService.currentUser?.uid ?: "unknown"

    suspend fun fetchAllFactories(): List<RemoteResultFactoryModel> {
        return try {
            val documents = db.getCollection(
                "users/$userId/$FACTORIES",
                RemoteResultFactoryModel.serializer()
            )
            println("on fetchAllFactories in firestore: $documents")
            documents
        } catch (e: Exception) {
            println("Firestore : on firestore getFactories: $e")
            emptyList()
        }
    }
}
