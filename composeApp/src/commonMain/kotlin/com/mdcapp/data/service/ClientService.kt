package com.mdcapp.data.service

import com.mdcapp.data.remote.RemoteResultClientModel
import dev.gitlive.firebase.firestore.FirebaseFirestore

class ClientService(
    private val db: FirebaseFirestore,
    private val authService: AuthService
) {
    companion object {
        const val CLIENTS = "clients"
    }

    private val userId: String
        get() = authService.currentUser?.uid ?: "unknown"

    private val clientsCollection
        get() = db.collection("users").document(userId).collection(CLIENTS)

    private var lastDocumentId: String? = null
    private var hasMore = true

    fun resetPagination() {
        lastDocumentId = null
        hasMore = true
    }

    suspend fun fetchClientsPaged(limit: Long): Pair<List<RemoteResultClientModel>, Boolean> {
        return try {
            if (!hasMore) return emptyList<RemoteResultClientModel>() to false

            var query = clientsCollection
                .orderBy("Razón Social")
                .limit(limit)

            lastDocumentId?.let { docId ->
                val lastDoc = clientsCollection.document(docId).get()
                if (lastDoc.exists) {
                    query = query.startAfter(lastDoc)
                }
            }
// ...

            val snapshot = query.get()

            if (snapshot.documents.isEmpty()) {
                hasMore = false
                return emptyList<RemoteResultClientModel>() to false
            }

            lastDocumentId = snapshot.documents.lastOrNull()?.id

            val items = snapshot.documents.mapNotNull { doc ->
                try {
                    doc.data<RemoteResultClientModel>()
                } catch (e: Exception) {
                    null
                }
            }

            items to true
        } catch (e: Exception) {
            println("Error en fetchClientsPaged: ${e.message}")
            emptyList<RemoteResultClientModel>() to false
        }
    }

    suspend fun fetchAmountClients(): Long {
        return try {
            val snapshot = clientsCollection.get()
            snapshot.documents.size.toLong()
        } catch (e: Exception) {
            println("Error en fetchAmountClients: ${e.message}")
            0L
        }
    }

    suspend fun searchClientsByName(query: String): List<RemoteResultClientModel> {
        val searchTerm = query.trim().lowercase()

        return try {
            val snapshot = clientsCollection.get()

            snapshot.documents.mapNotNull { doc ->
                try {
                    doc.data<RemoteResultClientModel>()
                } catch (e: Exception) {
                    println("firestore -> Error mapping client: $e")
                    null
                }
            }.filter { client ->
                if (searchTerm.isEmpty()) true
                else client.clientName.lowercase().contains(searchTerm)
            }
        } catch (e: Exception) {
            println("firestore -> Error fetching clients: $e")
            emptyList()
        }
    }

    suspend fun fetchClientName(clientId: String): RemoteResultClientModel {
        return try {
            val snapshot = clientsCollection.document(clientId).get()
            val client = RemoteResultClientModel(
                clientId = snapshot.get("Cliente Id") ?: "",
                clientName = snapshot.get("Razón Social") ?: ""
            )

            println("ClientService -> fetClientName: $client")
            client
        } catch (e: Exception) {
            println("firestore -> Error fetching client name: $e")
            RemoteResultClientModel("", "")
        }
    }

    suspend fun fetchAllClientsName(): List<RemoteResultClientModel> {
        return try {
            val snapshot = clientsCollection.get()
            val clientList = snapshot.documents.mapNotNull { doc ->
                try {
                    RemoteResultClientModel(
                        clientId = doc.get("Cliente Id") ?: "",
                        clientName = doc.get("Razón Social") ?: ""
                    )
                } catch (e: Exception) {
                    println("firestore -> Error mapping client")
                    null
                }
            }
            println("ClientService -> fetchAllClientsName: $clientList")
            clientList
        } catch (e: Exception) {
            println("firestore -> Error fetching clients: $e")
            emptyList()
        }
    }

    suspend fun saveClient(client: RemoteResultClientModel): Boolean {
        return try {
            if (client.clientId.isEmpty()) {
                val docRef = clientsCollection.add(client)
                docRef.update(mapOf("Cliente Id" to docRef.id))
            } else {
                clientsCollection.document(client.clientId).set(client)
            }
            true
        } catch (e: Exception) {
            println("Error saving client: ${e.message}")
            false
        }
    }

    suspend fun deleteClient(clientId: String): Boolean {
        return try {
            clientsCollection.document(clientId).delete()
            true
        } catch (e: Exception) {
            println("Error deleting client: ${e.message}")
            false
        }
    }
}

