package com.mdcapp.data.service

import android.util.Log
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.mdcapp.data.remote.RemoteResultBillingModel
import kotlinx.coroutines.tasks.await

class BillingPaginationService(
    private val db: FirebaseFirestore
) {
    companion object {
        const val BILLINGS = "billings"
        const val CLIENTS = "clients"

        val BILLING_OBJECT = RemoteResultBillingModel::class.java
    }

    private var lastDocumentSnapshot: DocumentSnapshot? = null
    private var hasMore = true

    fun resetPagination() {
        lastDocumentSnapshot = null
        hasMore = true

    }

    suspend fun fetchBillingsPaged(
        state: String,
        limit: Long
    ): Pair<List<RemoteResultBillingModel>, Boolean> {
        return try {
            if (!hasMore) return emptyList<RemoteResultBillingModel>() to false

            val query = db.collection(BILLINGS)
                .orderBy("Fecha")
                .whereEqualTo("Estado", state)
                .let { if (lastDocumentSnapshot != null) it.startAfter(lastDocumentSnapshot!!) else it }

            val snapshot = query.get().await()

            if (snapshot.documents.isEmpty()) {
                hasMore = false
                Log.i("firestore", "on fetchClients: empty")
                return emptyList<RemoteResultBillingModel>() to false
            }
            lastDocumentSnapshot = snapshot.last()

            val items =
                snapshot.documents.mapNotNull { it.toObject(BILLING_OBJECT) }
            Log.i("firestore", "on fetchClients: $items")
            items to true

        } catch (e: Exception) {
            Log.e("firestore", "Error on fetchClients: $e")
            emptyList<RemoteResultBillingModel>() to false
        }
    }
}