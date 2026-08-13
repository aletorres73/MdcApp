package com.mdcapp.data.service

import com.mdcapp.data.remote.RemoteResultClientModel
import com.mdcapp.domain.repositories.IDatabaseRepository
import io.github.aakira.napier.Napier

class ClientService(
    private val db: IDatabaseRepository,
    private val authService: AuthService
) {
    companion object {
        const val CLIENTS = "clients"
    }

    private val userId: String
        get() = authService.currentUser?.uid ?: "unknown"

    private val clientsPath
        get() = "users/$userId/$CLIENTS"

    private var lastDocumentId: String? = null
    private var hasMore = true

    fun resetPagination() {
        lastDocumentId = null
        hasMore = true
    }

    suspend fun fetchClientsPaged(limit: Long): Pair<List<RemoteResultClientModel>, Boolean> {
        return try {
            if (!hasMore) return emptyList<RemoteResultClientModel>() to false

            val allClients = db.getCollection(clientsPath, RemoteResultClientModel.serializer())
                .sortedBy { it.clientName }

            val startIndex = if (lastDocumentId == null) 0 else {
                val index = allClients.indexOfFirst { it.clientId == lastDocumentId }
                if (index == -1) 0 else index + 1
            }

            val pagedItems = allClients.drop(startIndex).take(limit.toInt())

            if (pagedItems.isEmpty()) {
                hasMore = false
                return emptyList<RemoteResultClientModel>() to false
            }

            lastDocumentId = pagedItems.lastOrNull()?.clientId
            hasMore = allClients.size > startIndex + pagedItems.size

            pagedItems to hasMore
        } catch (e: Exception) {
            Napier.e("Error en fetchClientsPaged", e)
            emptyList<RemoteResultClientModel>() to false
        }
    }

    suspend fun fetchAmountClients(): Long {
        return try {
            db.getCollection(clientsPath, RemoteResultClientModel.serializer()).size.toLong()
        } catch (e: Exception) {
            Napier.e("Error en fetchAmountClients", e)
            0L
        }
    }

    suspend fun searchClientsByName(query: String): List<RemoteResultClientModel> {
        val searchTerm = query.trim().lowercase()

        return try {
            val allClients = db.getCollection(clientsPath, RemoteResultClientModel.serializer())

            allClients.filter { client ->
                if (searchTerm.isEmpty()) true
                else client.clientName.lowercase().contains(searchTerm)
            }
        } catch (e: Exception) {
            Napier.e("Error fetching clients", e)
            emptyList()
        }
    }

    suspend fun fetchClientName(clientId: String): RemoteResultClientModel {
        return try {
            val client =
                db.getDocument("$clientsPath/$clientId", RemoteResultClientModel.serializer())
            client ?: RemoteResultClientModel("", "")
        } catch (e: Exception) {
            Napier.e("Error fetching client name", e)
            RemoteResultClientModel("", "")
        }
    }

    suspend fun fetchAllClientsName(): List<RemoteResultClientModel> {
        return try {
            db.getCollection(clientsPath, RemoteResultClientModel.serializer())
        } catch (e: Exception) {
            Napier.e("Error fetching clients", e)
            emptyList()
        }
    }

    suspend fun saveClient(client: RemoteResultClientModel): Boolean {
        return try {
            if (client.clientId.isEmpty()) {
                val newId =
                    db.addDocument(clientsPath, client, RemoteResultClientModel.serializer())
                db.updateDocument<Any>(
                    "$clientsPath/$newId",
                    mapOf("Cliente Id" to newId)
                )
            } else {
                db.setDocument(
                    "$clientsPath/${client.clientId}",
                    client,
                    RemoteResultClientModel.serializer()
                )
            }
            true
        } catch (e: Exception) {
            Napier.e("Error saving client", e)
            false
        }
    }

    suspend fun deleteClient(clientId: String): Boolean {
        return try {
            db.deleteDocument("$clientsPath/$clientId")
            true
        } catch (e: Exception) {
            Napier.e("Error deleting client", e)
            true
        }
    }
}
