package com.mdcapp.domain.repositories

import com.mdcapp.data.remote.toBillingDomain
import com.mdcapp.data.remote.toBillingRemote
import com.mdcapp.data.remote.toBuyOrderDomain
import com.mdcapp.data.remote.toBuyOrderRemote
import com.mdcapp.data.remote.toFactoryDomain
import com.mdcapp.data.remote.toFactoryRemote
import com.mdcapp.data.remote.toOrderDomain
import com.mdcapp.data.remote.toPaymentConditions
import com.mdcapp.data.remote.toPaymentDomain
import com.mdcapp.data.remote.toPaymentRemote
import com.mdcapp.data.service.OrderService
import com.mdcapp.domain.entities.BillingModel
import com.mdcapp.domain.entities.BuyOrderModel
import com.mdcapp.domain.entities.FactoryModel
import com.mdcapp.domain.entities.OrderModel
import com.mdcapp.domain.entities.PaymentCondition
import com.mdcapp.domain.entities.PaymentRegisterModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class OrderRepository(private val service: OrderService) {
    fun refresh() {
        service.refresh()
    }

    suspend fun getAllOrders(): List<OrderModel> =
        service.fetchAllOrders().map { it.toOrderDomain() }

    suspend fun getBuyOrderById(clientId: String, orderId: String) =
        service.fetchBuyOrder(clientId, orderId).toBuyOrderDomain()

    suspend fun getBuyOrdersByClient(clientId: String) =
        service.fetchBuyOrdersByClient(clientId).map { it.toBuyOrderDomain() }

    suspend fun getBillingsByOrder(clientId: String, orderId: String) =
        service.fetchBillings(clientId, orderId).map { it.toBillingDomain() }

    suspend fun getBillingsByClientID(clientId: String) =
        service.fetchBillingsByClient(clientId).map { it.toBillingDomain() }

    suspend fun getBillingsByBrand(brand: String, clientId: String) =
        service.fetchBillingsByBrand(brand, clientId).map { it.toBillingDomain() }

    suspend fun getOrdersByFactory(name: String) =
        service.fetchOrdersByFactory(name).map { it.toOrderDomain() }

    suspend fun getPaymentsConditionFactory(factoryName: String) =
        service.fetchPaymentsTypesFactory(factoryName).toPaymentConditions()

    suspend fun getPaymentConditionByBrand(brand: String) =
        service.fetchPaymentConditionByBrand(brand).toPaymentConditions()

    suspend fun addPaymentConditionsToFactory(
        factoryName: String,
        data: List<PaymentCondition>
    ) = service.setPaymentsConditionsFactory(factoryName, data)

    suspend fun addPaymentToRegister(data: PaymentRegisterModel) =
        service.addPaymentToRegister(data.toPaymentRemote())

    suspend fun getLastId() = service.fetchLastIdFromPayments()

    suspend fun updateBillingOnService(
        clientId: String,
        orderId: String,
        documentId: String,
        data: BillingModel
    ) =
        service.updateBilling(clientId, orderId, documentId, data.toBillingRemote())

    suspend fun saveBilling(clientId: String, orderId: String, data: BillingModel) =
        service.saveBilling(clientId, orderId, data.toBillingRemote())

    suspend fun deleteBilling(invoiceNumber: String) =
        service.deleteBilling(invoiceNumber)

    suspend fun getPaymentsRegisterByNumberDocument(documentList: List<String>) =
        service.fetchPaymentRegisterByNumberList(documentList).map { it.toPaymentDomain() }

    suspend fun getOrderBranch(clientId: String, orderId: String) =
        service.fetchOrderBranch(clientId, orderId)

    suspend fun getFactoriesList() = service.fetchFactoriesLisName()

    suspend fun getInvoiceByNumber(invoiceNumber: String): BillingModel {
        return service.fetchInvoiceByNumber(invoiceNumber).toBillingDomain()
    }

    fun observeInvoiceByNumber(invoiceNumber: String): Flow<BillingModel> =
        service.observeInvoiceByNumber(invoiceNumber).map { it.toBillingDomain() }

    fun observeBillingsByClient(clientId: String): Flow<List<BillingModel>> =
        service.observeBillingsByClient(clientId).map { list -> list.map { it.toBillingDomain() } }

    fun observePaymentsByInvoice(invoiceNumber: String): Flow<List<PaymentRegisterModel>> =
        service.observePaymentsByInvoice(invoiceNumber)
            .map { list -> list.map { it.toPaymentDomain() } }

    fun observePaymentsByClient(clientId: String): Flow<List<PaymentRegisterModel>> =
        service.observePaymentsByClient(clientId)
            .map { list -> list.map { it.toPaymentDomain() } }

    fun observeAllBillings(): Flow<List<BillingModel>> =
        service.observeAllBillings().map { list -> list.map { it.toBillingDomain() } }

    fun observeAllPayments(): Flow<List<PaymentRegisterModel>> =
        service.observeAllPayments().map { list -> list.map { it.toPaymentDomain() } }

    fun observeFactories(): Flow<List<FactoryModel>> =
        service.observeAllFactories().map { list -> list.map { it.toFactoryDomain() } }

    fun observeBuyOrdersByClient(clientId: String): Flow<List<BuyOrderModel>> =
        service.observeBuyOrdersByClient(clientId)
            .map { list -> list.map { it.toBuyOrderDomain() } }

    fun observeAllOrders(): Flow<List<OrderModel>> =
        service.observeAllOrders().map { list -> list.map { it.toOrderDomain() } }

    fun observeOrdersByFactory(name: String): Flow<List<OrderModel>> =
        service.observeOrdersByFactory(name).map { list -> list.map { it.toOrderDomain() } }

    suspend fun saveOrder(clientId: String, order: BuyOrderModel): Boolean {
        return service.saveOrder(clientId, order.toBuyOrderRemote())
    }

    suspend fun updateOrder(clientId: String, order: BuyOrderModel): Boolean {
        return service.updateOrder(clientId, order.order, order.toBuyOrderRemote())
    }

    suspend fun saveFactory(factory: FactoryModel): Boolean {
        return service.saveFactory(factory.toFactoryRemote())
    }

    suspend fun deleteFactory(factoryName: String): Boolean {
        return service.deleteFactory(factoryName)
    }

    suspend fun getFactories(): List<FactoryModel> {
        return service.fetchAllFactories().map { it.toFactoryDomain() }
    }
}

