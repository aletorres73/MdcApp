package com.mdcapp.domain.usescases.invoiceusecase

import com.mdcapp.data.model.BillingModel
import com.mdcapp.data.model.ClientModel
import com.mdcapp.data.model.toDomain
import com.mdcapp.data.service.ClientService
import com.mdcapp.domain.repositories.OrderRepository

class InvoiceUseCase(private val repository: OrderRepository, private val service: ClientService) {

    inner class GetBillingsByClient {
        suspend operator fun invoke(clientId: String): List<BillingModel> {
            return repository.getBillingsByClientID(clientId)
        }
    }

    inner class GetClientName {
        suspend operator fun invoke(clientId: String): ClientModel {
            return service.fetchClientName(clientId).toDomain()
        }
    }

    inner class FilterByBrand {
        suspend operator fun invoke(brand: String, clientId: String) =
            repository.getBillingsByBrand(brand, clientId)
    }

    inner class GetInvoiceByNumber {
        suspend operator fun invoke(invoiceNumber: String) =
            repository.getInvoiceByNumber(invoiceNumber)
    }
}