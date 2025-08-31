package com.mdcapp.domain.usescases.clientsusecase

import com.mdcapp.data.model.ClientModel
import com.mdcapp.data.model.toDomain
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

    fun resetPagination() {
        currentPage = 0
        service.resetPagination()
    }
}