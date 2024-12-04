@file:Suppress("INLINE_FROM_HIGHER_PLATFORM")

package com.mdcapp.domain.service

import com.mdcapp.data.remote.RemoteResultOrder
import dev.gitlive.firebase.firestore.FirebaseFirestore

class OrderService(
    private val db: FirebaseFirestore
) {
    suspend fun fetchAllOrders(): List<RemoteResultOrder> {
        return try {
            val documents = db.collection("buyOrders")
                .get()
                .documents
                .map { it.data<RemoteResultOrder>() }
            println("on fetchAllOrders in firestore: $documents")
            println("on fetchAllOrders in firestore data size: ${documents.size}")

            documents
        } catch (e: Exception) {
            println("Firestore : on firestore getCollections: $e")
            emptyList()
        }
    }
}
