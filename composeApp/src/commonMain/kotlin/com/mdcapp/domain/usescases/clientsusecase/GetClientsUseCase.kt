package com.mdcapp.domain.usescases.clientsusecase

import com.mdcapp.data.remote.toClientDomain
import com.mdcapp.data.remote.toClientRemote
import com.mdcapp.data.service.ClientService
import com.mdcapp.domain.entities.ClientModel
import kotlinx.coroutines.flow.map

class GetClientsUseCase(
    private val service: ClientService
) {
    private var currentPage = 0

    suspend operator fun invoke(): Pair<List<ClientModel>, Boolean> {
        val pageSize = 15
        val (items, result) = service.fetchClientsPaged(pageSize.toLong())
        currentPage++
        val hasMore = items.size == pageSize
        return items.map { it.toClientDomain() } to hasMore

    }

    suspend fun getAmountClients(): Long {
        return service.fetchAmountClients()
    }

    fun resetPagination() {
        currentPage = 0
        service.resetPagination()
    }

    suspend fun search(clientName: String): List<ClientModel> {
        return service.searchClientsByName(clientName).map { it.toClientDomain() }
    }

    suspend fun save(client: ClientModel): Boolean {
        return service.saveClient(client.toClientRemote())
    }

    suspend fun delete(clientId: String): Boolean {
        // Marcamos al cliente como inactivo
        return service.deleteClient(clientId)
    }

    suspend fun restore(clientId: String): Boolean {
        return service.restoreClient(clientId)
    }

    suspend fun getAll(): List<ClientModel> {
        return service.fetchAllClientsName().map { it.toClientDomain() }
    }

    fun observeAll(): kotlinx.coroutines.flow.Flow<List<ClientModel>> {
        return service.observeAllClients().map { list -> list.map { it.toClientDomain() } }
    }

    suspend fun getNextId(): String {
        return service.peekNextClientNumber()
    }

    suspend fun getClientById(clientId: String): ClientModel? {
        return service.fetchClientById(clientId)?.toClientDomain()
    }
}

