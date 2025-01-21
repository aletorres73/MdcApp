package com.mdcapp.domain.remote

import com.mdcapp.data.model.BillingModel
import com.mdcapp.data.model.BuyOrderModel
import com.mdcapp.data.model.OrderModel
import com.mdcapp.data.model.PaymentCondition
import com.mdcapp.data.remote.toDomain
import com.mdcapp.data.remote.toPaymentConditions
import com.mdcapp.domain.service.OrderService

class OrderRepository(private val service: OrderService) {
    suspend fun getAllOrders(): List<OrderModel> {
        return service.fetchAllOrders().map { it.toDomain() }
    }

    suspend fun getBuyOrderById(orderId: String): BuyOrderModel {
        return service.fetchBuyOrder(orderId).toDomain()
    }

    suspend fun getBillingsByOrder(orderId: String): List<BillingModel> {
        return service.fetchBillings(orderId).map { it.toDomain() }
    }

    suspend fun getOrdersByFactory(name: String): List<OrderModel> {
        return service.fetchOrdersByFactory(name).map { it.toDomain() }
    }

    suspend fun getPaymentsConditionFactory(factoryName: String): List<PaymentCondition> {
        return service.fetchPaymentsTypesFactory(factoryName).toPaymentConditions()
    }
}