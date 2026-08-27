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

    suspend fun getNextClientNumber(): String {
        val path = "users/$userId/config/counters"
        return try {
            val config = db.getDocument(path, CounterConfig.serializer())

            // 🛡️ REFUERZO: Si el documento no existe O si el contador está en 0, 
            // forzamos un escaneo de la base de datos para sincronizar.
            val next = if (config == null || config.lastClientNumber == 0) {
                Napier.w("Contador no inicializado (valor 0 o null). Sincronizando con clientes existentes...")

                val allModels = fetchAllClientsName()
                val allIds = db.getCollectionIds(clientsPath)

                val modelIds = allModels.mapNotNull { it.clientId.trim().toIntOrNull() }
                val docIds = allIds.mapNotNull { it.trim().toIntOrNull() }

                val maxId = (modelIds + docIds).maxOrNull() ?: 0

                val startFrom = maxId + 1
                Napier.i("Sincronización: Máximo ID detectado: $maxId. Próximo ID: $startFrom")
                startFrom
            } else {
                config.lastClientNumber + 1
            }

            db.setDocument(path, CounterConfig(lastClientNumber = next), CounterConfig.serializer())
            next.toString()
        } catch (e: Exception) {
            Napier.e("Error crítico en getNextClientNumber", e)
            "1" // Fallback mínimo
        }
    }

    suspend fun peekNextClientNumber(): String {
        val path = "users/$userId/config/counters"
        return try {
            val config = db.getDocument(path, CounterConfig.serializer())

            // Si no existe o es 0, hacemos el peek basado en el escaneo real
            if (config == null || config.lastClientNumber == 0) {
                val allModels = fetchAllClientsName()
                val allIds = db.getCollectionIds(clientsPath)
                val maxId = (allModels.mapNotNull { it.clientId.trim().toIntOrNull() } +
                        allIds.mapNotNull { it.trim().toIntOrNull() }).maxOrNull() ?: 0
                (maxId + 1).toString()
            } else {
                (config.lastClientNumber + 1).toString()
            }
        } catch (e: Exception) {
            "..."
        }
    }

    suspend fun saveClient(client: RemoteResultClientModel): Boolean {
        return try {
            val finalClient = if (client.clientId.isEmpty()) {
                val newId = getNextClientNumber()
                client.copy(clientId = newId)
            } else {
                client
            }

            db.setDocument(
                "$clientsPath/${finalClient.clientId}",
                finalClient,
                RemoteResultClientModel.serializer()
            )
            true
        } catch (e: Exception) {
            Napier.e("Error saving client", e)
            false
        }
    }

    @kotlinx.serialization.Serializable
    private data class CounterConfig(val lastClientNumber: Int = 0)

    suspend fun deleteClient(clientId: String): Boolean {
        return try {
            db.deleteDocument("$clientsPath/$clientId")

            // Intentar disminuir el contador si el cliente eliminado era el último
            val path = "users/$userId/config/counters"
            val config = db.getDocument(path, CounterConfig.serializer())
            val currentLast = config?.lastClientNumber ?: 0
            val deletedIdInt = clientId.toIntOrNull()

            if (deletedIdInt != null && deletedIdInt == currentLast) {
                val newLast = (currentLast - 1).coerceAtLeast(0)
                db.setDocument(
                    path,
                    CounterConfig(lastClientNumber = newLast),
                    CounterConfig.serializer()
                )
            }

            true
        } catch (e: Exception) {
            Napier.e("Error deleting client", e)
            true
        }
    }
}
