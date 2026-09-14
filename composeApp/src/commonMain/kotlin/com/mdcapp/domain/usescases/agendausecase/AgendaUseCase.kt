package com.mdcapp.domain.usescases.agendausecase

import com.mdcapp.domain.entities.BillingModel
import com.mdcapp.domain.repositories.OrderRepository

class AgendaUseCase(private val repository: OrderRepository) {
    inner class FetchAgenda {
        suspend operator fun invoke(dateFrom: Long, dateTo: Long): List<BillingModel> {
            return repository.fetchAgendaBillings(dateFrom, dateTo)
                .filter { it.rest > 0 }
        }
    }
}
