package com.mdcapp.domain.usescases.ordersusescases

import com.mdcapp.domain.entities.BillingModel
import com.mdcapp.domain.entities.BuyOrderModel
import com.mdcapp.domain.entities.PaymentRegisterModel
import com.mdcapp.domain.repositories.OrderRepository
import kotlinx.coroutines.flow.Flow

class BuyOrderUseCase(private val repository: OrderRepository) {
    inner class GetBuyOrderById {
        suspend operator fun invoke(clientId: String, orderId: String): BuyOrderModel {
            return repository.getBuyOrderById(clientId, orderId)
        }
    }

    inner class GetBuyOrdersByClient {
        suspend operator fun invoke(clientId: String): List<BuyOrderModel> {
            return repository.getBuyOrdersByClient(clientId)
        }
    }

    inner class ObserveBuyOrdersByClient {
        operator fun invoke(clientId: String): Flow<List<BuyOrderModel>> {
            return repository.observeBuyOrdersByClient(clientId)
        }
    }

    inner class GetBillings {
        suspend operator fun invoke(clientId: String, orderId: String): List<BillingModel> {
            return repository.getBillingsByOrder(clientId, orderId)
        }
    }

    inner class AddPaymentToRegister {
        suspend operator fun invoke(data: PaymentRegisterModel): Boolean {
            return repository.addPaymentToRegister(data)
        }
    }

    inner class GetLastIdPaymentFromRegister {
        suspend operator fun invoke(): Int {
            return repository.getLastId()
        }
    }

    inner class UpdateBilling {
        suspend operator fun invoke(
            clientId: String,
            orderId: String,
            numberBilling: String,
            billingModel: BillingModel
        ): Boolean {
            return repository.updateBillingOnService(clientId, orderId, numberBilling, billingModel)
        }
    }

    inner class GetPaymentsRegister {
        suspend operator fun invoke(documentList: List<String>): List<PaymentRegisterModel> {
            return repository.getPaymentsRegisterByNumberDocument(documentList)
        }

        suspend operator fun invoke(clientId: String) {}
    }

    inner class SaveOrder {
        suspend operator fun invoke(clientId: String, order: BuyOrderModel): Boolean {
            return repository.saveOrder(clientId, order)
        }
    }

    inner class UpdateOrder {
        suspend operator fun invoke(clientId: String, order: BuyOrderModel): Boolean {
            return repository.updateOrder(clientId, order)
        }
    }
}

