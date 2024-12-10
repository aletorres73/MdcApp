package com.mdcapp.domain.usescases.ordersusescases

import com.mdcapp.data.model.BillingModel
import com.mdcapp.data.model.BuyOrderModel
import com.mdcapp.domain.remote.OrderRepository

class BuyOrderUseCase(private val repository: OrderRepository) {
    inner class GetBuyOrderById {
        suspend operator fun invoke(orderId: String): BuyOrderModel {
            return repository.getBuyOrderById(orderId)
        }
    }

    inner class GetBillings {
        suspend operator fun invoke(orderId: String): List<BillingModel> {
            return repository.getBillingsByOrder(orderId)
        }
    }
}