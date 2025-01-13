@file:Suppress("INLINE_FROM_HIGHER_PLATFORM")

package com.mdcapp.domain.service

import com.mdcapp.data.remote.RemoteResultBillingModel
import com.mdcapp.data.remote.RemoteResultBuyOrder
import com.mdcapp.data.remote.RemoteResultOrder
import dev.gitlive.firebase.firestore.FieldPath
import dev.gitlive.firebase.firestore.FirebaseFirestore

class OrderService(
    private val db: FirebaseFirestore
) {
    companion object {
        const val ORDERS = "Orders"
        const val BUY_ORDERS = "buyOrders"
        const val BILLINGS = "billings"
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

    suspend fun fetchOrdersByFactory(name: String): List<RemoteResultOrder> {
        return try {
            val document = db.collection(ORDERS)
                .where { FieldPath("Marca").equalTo(name) }
                .get()
                .documents
                .map { it.data<RemoteResultOrder>() }
            document

        } catch (e: Exception) {
            println("Firestor: on firesotroe get orders by factories $e")
            emptyList()
        }
    }

    suspend fun fetchBuyOrder(orderId: String): RemoteResultBuyOrder {
        return try {
            val document = db.collection(BUY_ORDERS)
                .where { FieldPath("Orden Id").equalTo(orderId) }
                .get()
                .documents
                .map { it.data<RemoteResultBuyOrder>() }
                .first()
            println("on fetchBuyOrder in firestore: $document")
            document
        } catch (e: Exception) {
            println("Firestore : on firestore fetchBuyOrder: $e")
            RemoteResultBuyOrder()
        }
    }

    suspend fun fetchBillings(orderId: String): List<RemoteResultBillingModel> {
        return try {
            val document = db.collection(BILLINGS)
                .where { FieldPath("Orden").equalTo(orderId) }
                .get()
                .documents
                .map { it.data<RemoteResultBillingModel>() }
            println("on fetchBillings in firestore : $document")
            document
        } catch (e: Exception) {
            println("Firestore: on fetchBillings $e")
            emptyList()
        }
    }
}
