package com.mdcapp.domain.repositories

import com.mdcapp.data.model.BillingModel
import com.mdcapp.data.model.OrderModel
import com.mdcapp.data.model.PaymentCondition
import com.mdcapp.data.model.PaymentRegisterModel
import com.mdcapp.data.model.toDomain
import com.mdcapp.data.model.toRemote
import com.mdcapp.data.remote.toDomain
import com.mdcapp.data.remote.toPaymentConditions
import com.mdcapp.data.service.OrderService

class OrderRepository(private val service: OrderService) {
    suspend fun getAllOrders(): List<OrderModel> = service.fetchAllOrders().map { it.toDomain() }

    suspend fun getBuyOrderById(orderId: String) = service.fetchBuyOrder(orderId).toDomain()

    suspend fun getBillingsByOrder(orderId: String) =
        service.fetchBillings(orderId).map { it.toDomain() }

    suspend fun getBillingsByClientID(clientId: String) =
        service.fetchBillingsByClient(clientId).map { it.toDomain() }

    suspend fun getBillingsByBrand(brand: String, clientId: String) =
        service.fetchBillingsByBrand(brand, clientId).map { it.toDomain() }

    suspend fun getOrdersByFactory(name: String) =
        service.fetchOrdersByFactory(name).map { it.toDomain() }

    suspend fun getPaymentsConditionFactory(factoryName: String) =
        service.fetchPaymentsTypesFactory(factoryName).toPaymentConditions()

    suspend fun getPaymentConditionByBrand(brand: String) =
        service.fetchPaymentConditionByBrand(brand).toPaymentConditions()

    suspend fun addPaymentConditionsToFactory(
        factoryName: String,
        data: List<PaymentCondition>
    ) = service.setPaymentsConditionsFactory(factoryName, data)

    suspend fun addPaymentToRegister(data: PaymentRegisterModel) =
        service.addPaymentToRegister(data.toDomain())

    suspend fun getLastId() = service.fetchLastIdFromPayments()

    suspend fun updateBillingOnService(documentId: String, data: BillingModel) =
        service.updateBilling(documentId, data.toRemote())

    suspend fun getPaymentsRegisterByNumberDocument(documentList: List<String>) =
        service.fetchPaymentRegisterByNumberList(documentList).map { it.toDomain() }

    suspend fun getOrderBranch(orderId: String) = service.fetchOrderBranch(orderId)

    suspend fun getFactoriesList() = service.fetchFactoriesLisName()

    suspend fun getInvoiceByNumber(invoiceNumber: String): BillingModel {
        return service.fetchInvoiceByNumber(invoiceNumber).toDomain()
    }
}