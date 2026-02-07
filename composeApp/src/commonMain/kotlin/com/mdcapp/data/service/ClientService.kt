package com.mdcapp.data.service

import android.util.Log
import com.google.firebase.firestore.AggregateSource
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

    suspend fun fetchAmountClients(): Long {
        return try {
            val snapshot = db.collection(CLIENTS)
                .count()
                .get(AggregateSource.SERVER)
                .await()

            snapshot.count
        } catch (e: Exception) {
            Log.e("firestore", "Error on fetchClients: $e")
            0
        }
    }

    suspend fun searchClientsByName(query: String): List<RemoteResultClientModel> {
        val searchTerm = query.trim().lowercase()
        if (searchTerm.isEmpty()) return emptyList()

        return try {
            val snapshot = db.collection(CLIENTS).get().await()

            snapshot.documents.mapNotNull { doc ->
                try {
                    RemoteResultClientModel(
                        clientId = doc.getString("Cliente Id") ?: "",
                        clientName = doc.getString("Razon Social") ?: ""
                    )
                } catch (e: Exception) {
                    Log.e("firestore", "Error mapping client: $e")
                    null
                }
            }.filter { client ->
                client.clientName.lowercase().contains(searchTerm)
            }
        } catch (e: Exception) {
            Log.e("firestore", "Error fetching clients: $e")
            emptyList()
        }
    }

    suspend fun fetchClientName(clientId: String): RemoteResultClientModel {
        return try {
            val snapshot = db.collection(CLIENTS).document(clientId).get().await()
            val client = RemoteResultClientModel(
                clientId = snapshot.getString("Cliente Id") ?: "",
                clientName = snapshot.getString("Razon Social") ?: ""
            )

            Log.i("ClientService", "fetClientName: $client")
            client
        } catch (e: Exception) {
            Log.e("firestore", "Error fetching client name: $e")
            RemoteResultClientModel("", "")
        }
    }

    suspend fun fetchAllClientsName(): List<RemoteResultClientModel> {
        return try {
            val snapshot = db.collection(CLIENTS).get().await()
            val clientList = snapshot.documents.mapNotNull { doc ->
                try {
                    RemoteResultClientModel(
                        clientId = doc.getString("Cliente Id") ?: "",
                        clientName = doc.getString("Razon Social") ?: ""
                    )
                } catch (e: Exception) {
                    Log.e("firestore", "Error mapping client")
                    null
                }
            }
            Log.i("ClientService", "fetchAllClientsName: $clientList")
            clientList
        } catch (e: Exception) {
            Log.e("firestore", "Error fetching clients: $e")
            emptyList()
        }
    }

}