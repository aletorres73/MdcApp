@file:Suppress("INLINE_FROM_HIGHER_PLATFORM")

package com.mdcapp.domain.service

import com.mdcapp.data.remote.RemoteResultBuyOrder
import com.mdcapp.data.remote.RemoteResultOrder
import dev.gitlive.firebase.firestore.FirebaseFirestore

class OrderService(
    private val db: FirebaseFirestore
) {
    companion object {
        const val ORDERS = "Orders"
        const val BUY_ORDERS = "buyOrders"
    }

    suspend fun fetchAllOrders(): List<RemoteResultOrder> {
        return try {
            val documents = db.collection(ORDERS)
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

    suspend fun fetchBuyOrder(orderId: String): RemoteResultBuyOrder {
        return try {
            val document = db.collection(BUY_ORDERS)
                .document(orderId)
                .get()
                .data<RemoteResultBuyOrder>()
            println("on fetchBuyOrder in firestore: $document")
            document
        } catch (e: Exception) {
            println("Firestore : on firestore getCollections: $e")
            RemoteResultBuyOrder()
        }
    }
}
