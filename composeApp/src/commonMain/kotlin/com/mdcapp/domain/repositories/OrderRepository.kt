package com.mdcapp.domain.repositories

import com.mdcapp.data.model.BillingModel
import com.mdcapp.data.model.BuyOrderModel
import com.mdcapp.data.model.FactoryModel
import com.mdcapp.data.model.OrderModel
import com.mdcapp.data.model.PaymentCondition
import com.mdcapp.data.model.PaymentRegisterModel
import com.mdcapp.data.model.toDomain
import com.mdcapp.data.model.toRemote
import com.mdcapp.data.remote.toDomain
import com.mdcapp.data.remote.toPaymentConditions
import com.mdcapp.data.remote.toRemote
import com.mdcapp.data.service.OrderService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class OrderRepository(private val service: OrderService) {
    suspend fun getAllOrders(): List<OrderModel> = service.fetchAllOrders().map { it.toDomain() }

    suspend fun getBuyOrderById(clientId: String, orderId: String) =
        service.fetchBuyOrder(clientId, orderId).toDomain()

    suspend fun getBuyOrdersByClient(clientId: String) =
        service.fetchBuyOrdersByClient(clientId).map { it.toDomain() }

    suspend fun getBillingsByOrder(clientId: String, orderId: String) =
        service.fetchBillings(clientId, orderId).map { it.toDomain() }

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

    suspend fun updateBillingOnService(
        clientId: String,
        orderId: String,
        documentId: String,
        data: BillingModel
    ) =
        service.updateBilling(clientId, orderId, documentId, data.toRemote())

    suspend fun saveBilling(clientId: String, orderId: String, data: BillingModel) =
        service.saveBilling(clientId, orderId, data.toRemote())

    suspend fun getPaymentsRegisterByNumberDocument(documentList: List<String>) =
        service.fetchPaymentRegisterByNumberList(documentList).map { it.toDomain() }

    suspend fun getOrderBranch(clientId: String, orderId: String) =
        service.fetchOrderBranch(clientId, orderId)

    suspend fun getFactoriesList() = service.fetchFactoriesLisName()

    suspend fun getInvoiceByNumber(invoiceNumber: String): BillingModel {
        return service.fetchInvoiceByNumber(invoiceNumber).toDomain()
    }

    fun observeInvoiceByNumber(invoiceNumber: String): Flow<BillingModel> =
        service.observeInvoiceByNumber(invoiceNumber).map { it.toDomain() }

    fun observeBillingsByClient(clientId: String): Flow<List<BillingModel>> =
        service.observeBillingsByClient(clientId).map { list -> list.map { it.toDomain() } }

    fun observePaymentsByInvoice(invoiceNumber: String): Flow<List<PaymentRegisterModel>> =
        service.observePaymentsByInvoice(invoiceNumber).map { list -> list.map { it.toDomain() } }

    fun observeAllBillings(): Flow<List<BillingModel>> =
        service.observeAllBillings().map { list -> list.map { it.toDomain() } }

    fun observeBuyOrdersByClient(clientId: String): Flow<List<BuyOrderModel>> =
        service.observeBuyOrdersByClient(clientId).map { list -> list.map { it.toDomain() } }

    fun observeAllOrders(): Flow<List<OrderModel>> =
        service.observeAllOrders().map { list -> list.map { it.toDomain() } }

    fun observeOrdersByFactory(name: String): Flow<List<OrderModel>> =
        service.observeOrdersByFactory(name).map { list -> list.map { it.toDomain() } }

    suspend fun saveOrder(clientId: String, order: BuyOrderModel): Boolean {
        return service.saveOrder(clientId, order.toRemote())
    }

    suspend fun saveFactory(factory: FactoryModel): Boolean {
        return service.saveFactory(factory.toRemote())
    }

    suspend fun deleteFactory(factoryName: String): Boolean {
        return service.deleteFactory(factoryName)
    }

    suspend fun getFactories(): List<FactoryModel> {
        return service.fetchAllFactories().map { it.toDomain() }
    }
}
