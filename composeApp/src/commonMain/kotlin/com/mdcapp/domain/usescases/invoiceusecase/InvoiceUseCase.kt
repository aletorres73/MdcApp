package com.mdcapp.domain.usescases.invoiceusecase

import com.mdcapp.data.remote.toClientDomain
import com.mdcapp.data.service.BillingPaginationService
import com.mdcapp.data.service.ClientService
import com.mdcapp.domain.entities.BillingModel
import com.mdcapp.domain.entities.ClientModel
import com.mdcapp.domain.entities.InvoicePage
import com.mdcapp.domain.entities.PaymentCondition
import com.mdcapp.domain.entities.PaymentRegisterModel
import com.mdcapp.domain.repositories.OrderRepository
import kotlinx.coroutines.flow.Flow

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
            return service.fetchClientName(clientId).toClientDomain()
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

    inner class ObserveInvoice {
        operator fun invoke(invoiceNumber: String): Flow<BillingModel> {
            return repository.observeInvoiceByNumber(invoiceNumber)
        }
    }

    inner class ObserveBillingsByClient {
        operator fun invoke(clientId: String): Flow<List<BillingModel>> {
            return repository.observeBillingsByClient(clientId)
        }
    }

    inner class ObservePaymentsByInvoice {
        operator fun invoke(invoiceNumber: String): Flow<List<PaymentRegisterModel>> {
            return repository.observePaymentsByInvoice(invoiceNumber)
        }
    }

    inner class ObservePaymentsByClient {
        operator fun invoke(clientId: String): Flow<List<PaymentRegisterModel>> {
            return repository.observePaymentsByClient(clientId)
        }
    }

    inner class ObserveAllBillings {
        operator fun invoke(): Flow<List<BillingModel>> {
            return repository.observeAllBillings()
        }
    }

    inner class ObserveAllPayments {
        operator fun invoke(): Flow<List<PaymentRegisterModel>> {
            return repository.observeAllPayments()
        }
    }

    inner class UpdateInvoice {
        suspend operator fun invoke(
            clientId: String,
            orderId: String,
            invoiceNumber: String,
            data: BillingModel
        ): Boolean {
            return repository.updateBillingOnService(clientId, orderId, invoiceNumber, data)
        }
    }

    inner class CreateInvoice {
        suspend operator fun invoke(
            clientId: String,
            orderId: String,
            data: BillingModel
        ): Boolean {
            return repository.saveBilling(clientId, orderId, data)
        }
    }

    inner class DeleteInvoice {
        suspend operator fun invoke(invoiceNumber: String): Boolean {
            return repository.deleteBilling(invoiceNumber)
        }
    }

    inner class GetPaymentCondition {
        suspend operator fun invoke(brand: String, factory: String = ""): List<PaymentCondition> {
            val byBrand =
                if (brand.isNotEmpty()) repository.getPaymentConditionByBrand(brand) else emptyList()
            if (byBrand.isNotEmpty()) return byBrand

            return if (factory.isNotEmpty()) repository.getPaymentsConditionFactory(factory) else emptyList()
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
            return clientService.fetchAllClientsName().map { it.toClientDomain() }
        }
    }
}


