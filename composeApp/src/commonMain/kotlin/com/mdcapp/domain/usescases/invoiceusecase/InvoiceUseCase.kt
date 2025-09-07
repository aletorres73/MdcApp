package com.mdcapp.domain.usescases.invoiceusecase

import com.mdcapp.data.model.BillingModel
import com.mdcapp.domain.repositories.OrderRepository

class InvoiceUseCase(private val repository: OrderRepository) {

    inner class GetBillingsByClient {
        suspend operator fun invoke(clientId: String): List<BillingModel> {
            return repository.getBillingsByClientID(clientId)
        }
    }

}