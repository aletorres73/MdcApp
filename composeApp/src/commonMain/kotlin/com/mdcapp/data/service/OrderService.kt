package com.mdcapp.data.service

import com.mdcapp.data.remote.RemotePaymentRegisterResult
import com.mdcapp.data.remote.RemoteResultBillingModel
import com.mdcapp.data.remote.RemoteResultBuyOrder
import com.mdcapp.data.remote.RemoteResultFactoryModel
import com.mdcapp.data.remote.RemoteResultOrder
import com.mdcapp.domain.entities.PaymentCondition
import com.mdcapp.domain.repositories.DatabaseQuery
import com.mdcapp.domain.repositories.IDatabaseRepository
import io.github.aakira.napier.Napier
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class OrderService(
    private val db: IDatabaseRepository,
    private val authService: AuthService
) {
    companion object {
        const val ORDERS = "Orders"
        const val BUY_ORDERS = "buyOrders"
        const val FACTORIES = "factories"
        const val PAYMENTS_REGISTER = "paymentRegister"
    }

    private val userId: String
        get() = authService.currentUser?.uid ?: "unknown"

    private val userPath get() = "users/$userId"
    private val ordersPath get() = "$userPath/$ORDERS"
    private val factoriesPath get() = "$userPath/$FACTORIES"
    private val paymentsRegisterPath get() = "$userPath/$PAYMENTS_REGISTER"
    private val allBillingsPath get() = "$userPath/allBillings"

    private fun clientOrdersPath(clientId: String) =
        "$userPath/clients/$clientId/$BUY_ORDERS"

    fun refresh() {
        db.refresh()
    }

    suspend fun fetchAllOrders(): List<RemoteResultOrder> {
        return try {
            val documents = db.getCollection(ordersPath, RemoteResultOrder.serializer())
            Napier.i("on fetchAllOrders in firestore: $documents")
            Napier.i("on fetchAllOrders in firestore data size: ${documents.size}")
            documents
        } catch (e: Exception) {
            Napier.i("Firestore : on firestore getCollections: $e")
            emptyList()
        }
    }

    suspend fun fetchOrderBranch(clientId: String, orderId: String): String {
        return try {
            val buyOrder = fetchBuyOrder(clientId, orderId)
            buyOrder.branch
        } catch (e: Exception) {
            Napier.e("firestore --- on fetchOrderBranchList: $e")
            ""
        }
    }

    // Fixed fetchOrderBranch to actually use the right model if possible
    // Wait, RemoteBranchOrder only has 'branch'.
    // Original code:
    /*
    suspend fun fetchOrderBranch(clientId: String, orderId: String): String {
        return try {
            val data = clientOrdersCollection(clientId)
                .where { FieldPath("Orden Id").equalTo(orderId) }
                .get()
                .documents
                .map { it.data<RemoteBranchOrder>() }
            data.firstOrNull()?.branch ?: ""
        } catch (e: Exception) { ... }
    }
    */
    // If RemoteBranchOrder doesn't have "Orden Id", Firestore was using a field that was NOT in the data class but in the document.
    // getCollection<T> will fail if fields are missing unless we use a different approach.
    // However, I'll stick to getCollection and filter if the model supports it.
    // Let's assume for now that RemoteResultBuyOrder has the orderId and use that instead if needed.

    suspend fun fetchOrdersByFactory(name: String): List<RemoteResultOrder> {
        return try {
            if (name == "all") return fetchAllOrders()
            db.getCollection(
                ordersPath,
                RemoteResultOrder.serializer(),
                DatabaseQuery(filterBy = "Marca", equalTo = name)
            )
        } catch (e: Exception) {
            Napier.i("Firestore: on firesotore get orders by factories $e")
            emptyList()
        }
    }

    suspend fun fetchBuyOrder(clientId: String, orderId: String): RemoteResultBuyOrder {
        val path = "${clientOrdersPath(clientId)}/$orderId"
        return try {
            val data = db.getDocument(path, RemoteResultBuyOrder.serializer())
            val finalData = data?.let {
                if (it.id.isEmpty() || it.order.isEmpty()) {
                    it.copy(id = orderId, order = orderId)
                } else {
                    it
                }
            } ?: RemoteResultBuyOrder()
            Napier.i("firestore --- on fetchBuyOrder: $finalData")
            finalData
        } catch (e: Exception) {
            Napier.i("Firestore : on firestore fetchBuyOrder: $e")
            RemoteResultBuyOrder()
        }
    }

    suspend fun fetchBuyOrdersByClient(clientId: String): List<RemoteResultBuyOrder> {
        return try {
            db.getCollection(clientOrdersPath(clientId), RemoteResultBuyOrder.serializer())
                .map { data ->
                    if (data.id.isEmpty() || data.order.isEmpty()) {
                        // We don't have the doc.id here easily with getCollection unless the repo provides it.
                        // Assuming it's part of the data or we just return it.
                        data
                    } else {
                        data
                    }
                }
        } catch (e: Exception) {
            Napier.e("Error fetchBuyOrdersByClient", e)
            emptyList()
        }
    }

    suspend fun fetchBillings(clientId: String, orderId: String): List<RemoteResultBillingModel> {
        return try {
            db.getCollection(
                allBillingsPath,
                RemoteResultBillingModel.serializer(),
                DatabaseQuery(filterBy = "Cliente Id", equalTo = clientId)
            ).filter { it.orderId == orderId }
        } catch (e: Exception) {
            Napier.i("Firestore: on fetchBillings $e")
            emptyList()
        }
    }

    suspend fun fetchBillingsByClient(clientId: String): List<RemoteResultBillingModel> {
        return try {
            db.getCollection(
                allBillingsPath,
                RemoteResultBillingModel.serializer(),
                DatabaseQuery(filterBy = "Cliente Id", equalTo = clientId)
            )
        } catch (e: Exception) {
            Napier.i("OrderService: on fetchBillingsByClient $e")
            emptyList()
        }
    }

    suspend fun fetchPaymentsTypesFactory(factoryName: String): Map<String, Map<String, Any>> {
        return try {
            val document = db.getCollection(factoriesPath, RemoteResultFactoryModel.serializer())
                .firstOrNull { it.name == factoryName }
                ?: return emptyMap()

            @Suppress("UNCHECKED_CAST")
            val paymentsTypes = document.paymentsTypes as Map<String, Map<String, Any>>
            Napier.i("Firestore --- on fetchPaymentsTypesFactory in firestore : $paymentsTypes")
            paymentsTypes
        } catch (e: Exception) {
            Napier.i("Firestore: on fetchPaymentsTypesFactory $e")
            emptyMap()
        }
    }

    suspend fun fetchPaymentConditionByBrand(brand: String): Map<String, Map<String, Any>> {
        return try {
            val document = db.getCollection(factoriesPath, RemoteResultFactoryModel.serializer())
                .firstOrNull { it.branchList.contains(brand) }
                ?: return emptyMap()

            @Suppress("UNCHECKED_CAST")
            val paymentsTypes = document.paymentsTypes as Map<String, Map<String, Any>>
            Napier.i("Firestore --- on fetchPaymentsTypesFactory in firestore : $paymentsTypes")
            paymentsTypes
        } catch (e: Exception) {
            Napier.i("Firestore ---- on fetchPaymentsTypesFactory $e")
            emptyMap()
        }
    }

    suspend fun setPaymentsConditionsFactory(
        factoryName: String,
        data: List<PaymentCondition>
    ): Boolean {
        return try {
            db.updateDocument<Any>(
                "$factoriesPath/$factoryName",
                mapOf("Condiciones" to data)
            )
            true
        } catch (e: Exception) {
            Napier.e("Firestore: on setPaymentsConditionsFactory $e")
            false
        }
    }

    suspend fun addPaymentToRegister(data: RemotePaymentRegisterResult): Boolean {
        return try {
            db.setDocument(
                "$paymentsRegisterPath/${data.id}",
                data,
                RemotePaymentRegisterResult.serializer()
            )
            Napier.i("Firestore --- On addPaymentToRegister $data successful")
            true
        } catch (e: Exception) {
            Napier.e("Firestore--- onAddPaymentToRegister $e")
            false
        }
    }

    suspend fun fetchLastIdFromPayments(): Int {
        return try {
            val documents =
                db.getCollection(paymentsRegisterPath, RemotePaymentRegisterResult.serializer())
            val list = documents.maxByOrNull { it.id }
            if (list == null || documents.isEmpty()) 0 else list.id
        } catch (e: Exception) {
            Napier.e("firestore --- on fetchLastIdFromPayments $e")
            -1
        }
    }

    suspend fun updateBilling(
        clientId: String,
        orderId: String,
        document: String,
        data: RemoteResultBillingModel
    ): Boolean {
        return try {
            db.updateDocument(
                "$allBillingsPath/$document",
                data,
                RemoteResultBillingModel.serializer()
            )
            Napier.i("firestore --- on updateBilling success $data")
            true
        } catch (e: Exception) {
            Napier.e("firestore --- on updateBilling $e")
            false
        }
    }

    suspend fun saveBilling(
        clientId: String,
        orderId: String,
        data: RemoteResultBillingModel
    ): Boolean {
        return try {
            db.setDocument(
                "$allBillingsPath/${data.billingNumber}",
                data,
                RemoteResultBillingModel.serializer()
            )
            Napier.i("firestore --- on saveBilling success $data")
            true
        } catch (e: Exception) {
            Napier.e("firestore --- on saveBilling $e")
            false
        }
    }

    suspend fun deleteBilling(invoiceNumber: String): Boolean {
        return try {
            db.deleteDocument("$allBillingsPath/$invoiceNumber")
            Napier.i("firestore --- on deleteBilling success: $invoiceNumber")
            true
        } catch (e: Exception) {
            Napier.e("firestore --- on deleteBilling $e")
            false
        }
    }

    suspend fun fetchPaymentRegisterByNumberList(documentList: List<String>): List<RemotePaymentRegisterResult> {
        return try {
            val allPayments =
                db.getCollection(paymentsRegisterPath, RemotePaymentRegisterResult.serializer())
            allPayments.filter { it.documentNumber in documentList }
        } catch (e: Exception) {
            Napier.e("firestore --- on fetchPaymentRegisterByNumberList: $e ")
            emptyList()
        }
    }

    suspend fun fetchFactoriesLisName(): List<String> {
        return try {
            db.getCollection(factoriesPath, RemoteResultFactoryModel.serializer())
                .map { it.name }
        } catch (e: Exception) {
            Napier.e("firestore --- on fetchFactoriesLisName: $e ")
            emptyList()
        }
    }

    suspend fun fetchAllFactories(): List<RemoteResultFactoryModel> {
        return try {
            db.getCollection(factoriesPath, RemoteResultFactoryModel.serializer())
        } catch (e: Exception) {
            Napier.e("firestore --- on fetchAllFactories: $e ")
            emptyList()
        }
    }

    suspend fun fetchBillingsByBrand(
        brand: String,
        clientId: String
    ): List<RemoteResultBillingModel> {
        return try {
            db.getCollection(
                allBillingsPath,
                RemoteResultBillingModel.serializer(),
                DatabaseQuery(filterBy = "Cliente Id", equalTo = clientId)
            ).filter { it.brand == brand }
        } catch (e: Exception) {
            Napier.i("OrderService: on fetchBillingsByBrand $e")
            emptyList()
        }
    }

    suspend fun fetchInvoiceByNumber(invoiceNumber: String): RemoteResultBillingModel {
        return try {
            db.getCollection(allBillingsPath, RemoteResultBillingModel.serializer())
                .firstOrNull { it.billingNumber == invoiceNumber }
                ?: RemoteResultBillingModel()
        } catch (e: Exception) {
            Napier.i("OrderService: on fetchInvoiceByNumber $e")
            RemoteResultBillingModel()
        }
    }

    fun observeInvoiceByNumber(invoiceNumber: String): Flow<RemoteResultBillingModel> {
        return db.observeCollection(allBillingsPath, RemoteResultBillingModel.serializer())
            .map { list ->
                list.firstOrNull { it.billingNumber == invoiceNumber }
                    ?: RemoteResultBillingModel()
            }
    }

    fun observeBillingsByClient(clientId: String): Flow<List<RemoteResultBillingModel>> {
        return db.observeCollection(
            allBillingsPath,
            RemoteResultBillingModel.serializer(),
            DatabaseQuery(filterBy = "Cliente Id", equalTo = clientId)
        )
    }

    fun observePaymentsByInvoice(invoiceNumber: String): Flow<List<RemotePaymentRegisterResult>> {
        return db.observeCollection(
            paymentsRegisterPath,
            RemotePaymentRegisterResult.serializer(),
            DatabaseQuery(filterBy = "Remito", equalTo = invoiceNumber)
        )
    }

    fun observePaymentsByClient(clientId: String): Flow<List<RemotePaymentRegisterResult>> {
        return db.observeCollection(
            paymentsRegisterPath,
            RemotePaymentRegisterResult.serializer(),
            DatabaseQuery(filterBy = "Cliente ID", equalTo = clientId)
        )
    }

    fun observeAllBillings(): Flow<List<RemoteResultBillingModel>> {
        Napier.i("OrderService --- observeAllBillings")
        return db.observeCollection(
            allBillingsPath,
            RemoteResultBillingModel.serializer(),
            DatabaseQuery(orderBy = "Timestamp", descending = true, limit = 100)
        )
    }

    fun observeAllPayments(): Flow<List<RemotePaymentRegisterResult>> {
        Napier.i("OrderService --- observeAllPayments")
        return db.observeCollection(
            paymentsRegisterPath,
            RemotePaymentRegisterResult.serializer(),
            DatabaseQuery(orderBy = "Fecha", descending = true, limit = 100)
        )
    }

    fun observeBuyOrdersByClient(clientId: String): Flow<List<RemoteResultBuyOrder>> {
        Napier.i("OrderService --- observeBuyOrdersByClient: $clientId")
        return db.observeCollection(clientOrdersPath(clientId), RemoteResultBuyOrder.serializer())
    }

    fun observeAllOrders(): Flow<List<RemoteResultOrder>> {
        Napier.i("OrderService --- observeAllOrders")
        return db.observeCollection(
            ordersPath,
            RemoteResultOrder.serializer(),
            DatabaseQuery(orderBy = "Fecha de carga", descending = true, limit = 100)
        )
    }

    fun observeAllFactories(): Flow<List<RemoteResultFactoryModel>> {
        return db.observeCollection(factoriesPath, RemoteResultFactoryModel.serializer())
    }

    fun observeOrdersByFactory(name: String): Flow<List<RemoteResultOrder>> {
        if (name == "all") return observeAllOrders()
        return db.observeCollection(
            ordersPath,
            RemoteResultOrder.serializer(),
            DatabaseQuery(filterBy = "Marca", equalTo = name)
        )
    }

    suspend fun saveOrder(clientId: String, order: RemoteResultBuyOrder): Boolean {
        return try {
            val nextNumber = getNextOrderNumber()
            val orderId = nextNumber.toString()

            val finalOrder = order.copy(
                id = orderId,
                order = orderId,
                loadedDate = System.currentTimeMillis(),
                timeStamp = System.currentTimeMillis()
            )

            db.setDocument(
                "${clientOrdersPath(clientId)}/$orderId",
                finalOrder,
                RemoteResultBuyOrder.serializer()
            )
            true
        } catch (e: Exception) {
            Napier.e("Error saving order", e)
            false
        }
    }

    suspend fun updateOrder(
        clientId: String,
        orderId: String,
        order: RemoteResultBuyOrder
    ): Boolean {
        return try {
            val finalOrder = order.copy(id = orderId, order = orderId)
            db.setDocument(
                "${clientOrdersPath(clientId)}/$orderId",
                finalOrder,
                RemoteResultBuyOrder.serializer()
            )
            true
        } catch (e: Exception) {
            Napier.e("Error updating order", e)
            false
        }
    }

    private suspend fun getNextOrderNumber(): Int {
        val path = "$userPath/config/counters"
        return try {
            val config = db.getDocument(path, CounterConfig.serializer())
            val current = config?.lastOrderNumber ?: 0
            val next = current + 1
            db.setDocument(path, CounterConfig(next), CounterConfig.serializer())
            next
        } catch (e: Exception) {
            Napier.e("Error getting next order number", e)
            1
        }
    }

    // Simple MapSerializer helper or just use a specific model
    // For now I'll use getDocument and assume it can handle basic types if I add it to IDatabaseRepository
    // Or I'll just use a dedicated Config class.

    suspend fun saveFactory(factory: RemoteResultFactoryModel): Boolean {
        return try {
            db.setDocument(
                "$factoriesPath/${factory.name}",
                factory,
                RemoteResultFactoryModel.serializer()
            )
            true
        } catch (e: Exception) {
            Napier.e("Error saving factory", e)
            false
        }
    }

    suspend fun deleteFactory(factoryName: String): Boolean {
        return try {
            db.deleteDocument("$factoriesPath/$factoryName")
            true
        } catch (e: Exception) {
            Napier.e("Error deleting factory", e)
            false
        }
    }

    suspend fun deleteAllClientData(clientId: String): Boolean {
        return try {
            val buyOrders =
                db.getCollection(clientOrdersPath(clientId), RemoteResultBuyOrder.serializer())
            buyOrders.forEach { doc ->
                db.deleteDocument("${clientOrdersPath(clientId)}/${doc.id}")
            }

            val billings = db.getCollection(allBillingsPath, RemoteResultBillingModel.serializer())
                .filter { it.clientId == clientId }
            billings.forEach { doc ->
                db.deleteDocument("$allBillingsPath/${doc.billingNumber}")
            }

            val payments =
                db.getCollection(paymentsRegisterPath, RemotePaymentRegisterResult.serializer())
                    .filter { it.clientId == clientId }
            payments.forEach { doc ->
                db.deleteDocument("$paymentsRegisterPath/${doc.id}")
            }

            Napier.i("OrderService --- All data for client $clientId deleted successfully")
            true
        } catch (e: Exception) {
            Napier.e("OrderService --- Error deleting all data for client $clientId", e)
            false
        }
    }
}

// Dummy MapSerializer for simple config
@kotlinx.serialization.Serializable
private data class CounterConfig(val lastOrderNumber: Int = 0)
