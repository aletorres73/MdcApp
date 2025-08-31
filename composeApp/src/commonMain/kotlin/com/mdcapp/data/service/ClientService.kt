package com.mdcapp.data.service

import android.util.Log
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.mdcapp.data.model.RemoteResultClientModel
import kotlinx.coroutines.tasks.await

class ClientService(
    private val db: FirebaseFirestore
) {
    companion object {
        const val CLIENTS = "clients"
    }

    private var lastDocumentSnapshot: DocumentSnapshot? = null
    private var hasMore = true

    fun resetPagination() {
        lastDocumentSnapshot = null
        hasMore = true

    }

    suspend fun fetchClientsPaged(limit: Long): Pair<List<RemoteResultClientModel>, Boolean> {
        return try {
            if (!hasMore) return emptyList<RemoteResultClientModel>() to false

            val query: Query = db.collection(CLIENTS)
                .orderBy("Razon Social")
                .limit(limit)
                .let { if (lastDocumentSnapshot != null) it.startAfter(lastDocumentSnapshot!!) else it }

            val snapshot = query.get().await()

            if (snapshot.documents.isEmpty()) {
                hasMore = false
                Log.i("firestore", "on fetchClients: empty")
                return emptyList<RemoteResultClientModel>() to false

            }
            lastDocumentSnapshot = snapshot.last()


            val items = snapshot.documents.mapNotNull { doc ->
                try {
                    RemoteResultClientModel(
                        clientId = doc.getString("Cliente Id") ?: "",
                        clientName = doc.getString("Razon Social") ?: ""
                    )
                } catch (e: Exception) {
                    Log.e("firestore", "Error on fetchClients: $e")
                    null
                }
            }

            Log.i("firestore", "on fetchClients: $items")
            items to true


        } catch (e: Exception) {
            Log.e("firestore", "Error on fetchClients: $e")
            emptyList<RemoteResultClientModel>() to false
        }
    }
}