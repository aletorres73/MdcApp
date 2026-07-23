package com.mdcapp.domain.usescases.invoiceusecase

import com.mdcapp.data.model.BillingModel
import com.mdcapp.data.model.ClientModel
import com.mdcapp.data.model.PaymentCondition
import com.mdcapp.data.model.toDomain
import com.mdcapp.data.service.BillingPaginationService
import com.mdcapp.data.service.ClientService
import com.mdcapp.domain.entities.InvoicePage
import com.mdcapp.domain.repositories.OrderRepository

class InvoiceUseCase(
    private val repository: OrderRepository,
    private val service: ClientService,
    private val paginationService: BillingPaginationService,
    private val clientService: ClientService
) {

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
        suspend operator fun invoke(invoiceNumber: String): BillingModel {
            return repository.getInvoiceByNumber(invoiceNumber)
        }
    }

    inner class UpdateInvoice {
        suspend operator fun invoke(invoiceNumber: String, data: BillingModel): Boolean {
            return repository.updateBillingOnService(invoiceNumber, data)
        }
    }

    inner class CreateInvoice {
        suspend operator fun invoke(data: BillingModel): Boolean {
            return repository.saveBilling(data)
        }
    }

    inner class GetPaymentCondition {
        suspend operator fun invoke(brand: String): List<PaymentCondition> {
            return repository.getPaymentConditionByBrand(brand)
        }
    }

    inner class GetInvoicePaged {

        suspend fun loadNextPage(
            limit: Long,
            state: String,
            cursor: String? = null,
            direction: String = "desc",
            client: String? = null,
            number: String? = null
        ): Pair<InvoicePage, String?> {
            val page = paginationService.fetchBillingsPaged(
                state = state,
                limit = limit,
                startAfterId = cursor,
                direction = direction,
                client = client,
                number = number
            )
            return page to page.nextCursor
        }

        fun reset() = null
    }

    inner class GetAllClients {
        suspend operator fun invoke(): List<ClientModel> {
            return clientService.fetchAllClientsName().map { it.toDomain() }
        }
    }
}

