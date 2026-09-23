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

            val query = com.mdcapp.domain.repositories.DatabaseQuery(
                orderBy = "Razón Social",
                descending = false,
                limit = limit.toInt(),
                startAfter = lastDocumentId
            )

            val pagedItems =
                db.getCollection(clientsPath, RemoteResultClientModel.serializer(), query)

            if (pagedItems.isEmpty()) {
                hasMore = false
                return emptyList<RemoteResultClientModel>() to false
            }

            lastDocumentId = pagedItems.lastOrNull()?.clientName
            hasMore = pagedItems.size == limit.toInt()

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
        if (searchTerm.isEmpty()) return emptyList()

        return try {
            val dbQuery = com.mdcapp.domain.repositories.DatabaseQuery(
                filters = listOf(
                    com.mdcapp.domain.repositories.Filter(
                        "searchTerms",
                        "ARRAY_CONTAINS",
                        searchTerm
                    )
                ),
                limit = 20
            )
            db.getCollection(clientsPath, RemoteResultClientModel.serializer(), dbQuery)
        } catch (e: Exception) {
            Napier.e("Error searching clients", e)
            emptyList()
        }
    }

    suspend fun fetchClientById(clientId: String): RemoteResultClientModel? {
        return try {
            db.getDocument("$clientsPath/$clientId", RemoteResultClientModel.serializer())
        } catch (e: Exception) {
            Napier.e("Error fetching client by id", e)
            null
        }
    }

    suspend fun fetchClientName(clientId: String): RemoteResultClientModel {
        return fetchClientById(clientId) ?: RemoteResultClientModel("", "")
    }

    suspend fun fetchAllClientsName(): List<RemoteResultClientModel> {
        return try {
            db.getCollection(clientsPath, RemoteResultClientModel.serializer())
        } catch (e: Exception) {
            Napier.e("Error fetching clients", e)
            emptyList()
        }
    }

    fun observeAllClients(): kotlinx.coroutines.flow.Flow<List<RemoteResultClientModel>> {
        return db.observeCollection(clientsPath, RemoteResultClientModel.serializer())
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
            db.updateDocument<RemoteResultClientModel>(
                "$clientsPath/$clientId",
                mapOf("Activo" to false)
            )
            true
        } catch (e: Exception) {
            Napier.e("Error deactivating client", e)
            false
        }
    }

    suspend fun restoreClient(clientId: String): Boolean {
        return try {
            db.updateDocument<RemoteResultClientModel>(
                "$clientsPath/$clientId",
                mapOf("Activo" to true)
            )
            true
        } catch (e: Exception) {
            Napier.e("Error restoring client", e)
            false
        }
    }
}
