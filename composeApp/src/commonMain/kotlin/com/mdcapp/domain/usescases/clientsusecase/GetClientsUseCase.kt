package com.mdcapp.domain.usescases.clientsusecase

import com.mdcapp.data.model.ClientModel
import com.mdcapp.data.model.toDomain
import com.mdcapp.data.model.toRemote
import com.mdcapp.data.service.ClientService

class GetClientsUseCase(private val service: ClientService) {
    private var currentPage = 0

    suspend operator fun invoke(): Pair<List<ClientModel>, Boolean> {
        val pageSize = 15
        val (items, result) = service.fetchClientsPaged(pageSize.toLong())
        currentPage++
        val hasMore = items.size == pageSize
        return items.map { it.toDomain() } to hasMore

    }

    suspend fun getAmountClients(): Long {
        return service.fetchAmountClients()
    }

    fun resetPagination() {
        currentPage = 0
        service.resetPagination()
    }

    suspend fun search(clientName: String): List<ClientModel> {
        return service.searchClientsByName(clientName).map { it.toDomain() }
    }

    suspend fun save(client: ClientModel): Boolean {
        return service.saveClient(client.toRemote())
    }

    suspend fun delete(clientId: String): Boolean {
        return service.deleteClient(clientId)
    }

    suspend fun getAll(): List<ClientModel> {
        return service.fetchAllClientsName().map { it.toDomain() }
    }
}
