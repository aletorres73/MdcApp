package com.mdcapp.domain.remote

import com.mdcapp.data.model.BillingModel
import com.mdcapp.data.model.BuyOrderModel
import com.mdcapp.data.model.OrderModel
import com.mdcapp.data.model.PaymentCondition
import com.mdcapp.data.model.PaymentRegisterModel
import com.mdcapp.data.model.toDomain
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

    suspend fun addPaymentConditionsToFactory(
        factoryName: String,
        data: List<PaymentCondition>
    ): Boolean {
        return service.setPaymentsConditionsFactory(factoryName, data)
    }

    suspend fun addPaymentToRegister(data: PaymentRegisterModel): Boolean {
        return service.addPaymentToRegister(data.toDomain())
    }

    suspend fun getLastId(): Int {
        return service.fetchLastIdFromPayments()
    }

    suspend fun updateBillingOnService(documentId: String, data: BillingModel): Boolean {
        return service.updateBilling(documentId, data.toDomain())
    }

    suspend fun getPaymentsRegisterByNumberDocument(documentList: List<String>): List<PaymentRegisterModel> {
        return service.fetchPaymentRegisterByNumberList(documentList).map { it.toDomain() }
    }

    suspend fun getOrderBranch(orderId: String): String {
        return service.fetchOrderBranch(orderId)
    }
}