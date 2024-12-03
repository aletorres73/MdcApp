package com.mdcapp.domain.service

import com.mdcapp.data.remote.RemoteResultOrder
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.firestore.firestore

class OrderService(
//    private val db: FirebaseFirestore
) {
    private val db = Firebase.firestore
    suspend fun fetchAllOrders(): List<RemoteResultOrder> {
        return try {
            val documents = db.collection("buyOrders")
                .get()
                .documents
                .map{it.data<RemoteResultOrder>()}
            println("on fetchAllOrders in firestore: $documents")
            documents
        } catch (e: Exception) {
            println("Firestore : on firestore getCollections: $e")
            emptyList()
        }
    }
}
