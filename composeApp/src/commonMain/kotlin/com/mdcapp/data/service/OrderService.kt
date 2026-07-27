@file:Suppress("INLINE_FROM_HIGHER_PLATFORM")

package com.mdcapp.data.service

import android.util.Log
import com.mdcapp.data.model.PaymentCondition
import com.mdcapp.data.model.RemotePaymentRegisterResult
import com.mdcapp.data.remote.RemoteBranchOrder
import com.mdcapp.data.remote.RemoteResultBillingModel
import com.mdcapp.data.remote.RemoteResultBuyOrder
import com.mdcapp.data.remote.RemoteResultFactoryModel
import com.mdcapp.data.remote.RemoteResultOrder
import dev.gitlive.firebase.firestore.FieldPath
import dev.gitlive.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class OrderService(
    private val db: FirebaseFirestore,
    private val authService: AuthService
) {
    companion object {
        const val ORDERS = "Orders"
        const val BUY_ORDERS = "buyOrders"
        const val BILLINGS = "billings"
        const val FACTORIES = "factories"
        const val PAYMENTS_REGISTER = "paymentRegister"
    }

    private val userId: String
        get() = authService.currentUser?.uid ?: "unknown"

    private val userDoc
        get() = db.collection("users").document(userId)

    private val ordersCollection get() = userDoc.collection(ORDERS)
    private val buyOrdersCollection get() = userDoc.collection(BUY_ORDERS)
    private val factoriesCollection get() = userDoc.collection(FACTORIES)
    private val paymentsRegisterCollection get() = userDoc.collection(PAYMENTS_REGISTER)

    // Global Billings for Dashboard
    private val allBillingsCollection get() = userDoc.collection("allBillings")

    private fun clientOrdersCollection(clientId: String) =
        userDoc.collection("clients").document(clientId).collection(BUY_ORDERS)

    private fun orderBillingsCollection(clientId: String, orderId: String) =
        clientOrdersCollection(clientId).document(orderId).collection(BILLINGS)

    suspend fun fetchAllOrders(): List<RemoteResultOrder> {
// ...
        return try {
            val documents = ordersCollection
                .get()
                .documents
                .map { it.data<RemoteResultOrder>() }
            Log.i("MdcAppOnly", "on fetchAllOrders in firestore: $documents")
            Log.i("MdcAppOnly", "on fetchAllOrders in firestore data size: ${documents.size}")

            documents
        } catch (e: Exception) {
            Log.i("MdcAppOnly", "Firestore : on firestore getCollections: $e")
            emptyList()
        }
    }

    suspend fun fetchOrderBranch(clientId: String, orderId: String): String {
        return try {
            val data = clientOrdersCollection(clientId)
                .where { FieldPath("Orden Id").equalTo(orderId) }
                .get()
                .documents
                .map { it.data<RemoteBranchOrder>() }
            data.first().branch
        } catch (e: Exception) {
            Log.e("MdcAppOnly", "firestore --- on fetchOrderBranchList: $e")
            ""
        }
    }

    suspend fun fetchOrdersByFactory(name: String): List<RemoteResultOrder> {
        return try {
            if (name == "all") return fetchAllOrders()
            val document = ordersCollection
                .where { FieldPath("Marca").equalTo(name) }
                .get()
                .documents
                .map { it.data<RemoteResultOrder>() }
            document

        } catch (e: Exception) {
            Log.i("MdcAppOnly", "Firestore: on firesotore get orders by factories $e")
            emptyList()
        }
    }

    suspend fun fetchBuyOrder(clientId: String, orderId: String): RemoteResultBuyOrder {
        return try {
            val document = clientOrdersCollection(clientId).document(orderId).get()
            val data = document.data<RemoteResultBuyOrder>()
            Log.i("MdcAppOnly", "firestore --- on fetchBuyOrder: $data")
            data
        } catch (e: Exception) {
            Log.i("MdcAppOnly", "Firestore : on firestore fetchBuyOrder: $e")
            RemoteResultBuyOrder()
        }
    }

    suspend fun fetchBuyOrdersByClient(clientId: String): List<RemoteResultBuyOrder> {
        return try {
            val documents = clientOrdersCollection(clientId)
                .get()
                .documents
                .map { it.data<RemoteResultBuyOrder>() }
            documents
        } catch (e: Exception) {
            Log.e("OrderService", "Error fetchBuyOrdersByClient", e)
            emptyList()
        }
    }

    suspend fun fetchBillings(clientId: String, orderId: String): List<RemoteResultBillingModel> {
        return try {
            val document = orderBillingsCollection(clientId, orderId)
                .get()
                .documents
                .map { it.data<RemoteResultBillingModel>() }
            Log.i("MdcAppOnly", "firestore --- on fetchBillings: $document")
            document
        } catch (e: Exception) {
            Log.i("MdcAppOnly", "Firestore: on fetchBillings $e")
            emptyList()
        }
    }

    suspend fun fetchBillingsByClient(clientId: String): List<RemoteResultBillingModel> {
        return try {
            val document = allBillingsCollection
                .where { FieldPath("Cliente Id").equalTo(clientId) }
                .get()
                .documents
                .map { it.data<RemoteResultBillingModel>() }
            Log.i(
                "MdcAppOnly",
                "OrderService --- on fetchBillingsByClient in firestore : $document"
            )
            document
        } catch (e: Exception) {
            Log.i("MdcAppOnly", "OrderService: on fetchBillingsByClient $e")
            emptyList()
        }
    }

    suspend fun fetchPaymentsTypesFactory(factoryName: String): Map<String, Map<String, Any>> {
        return try {
            val document = factoriesCollection
                .where { FieldPath("Fabrica").equalTo(factoryName) }
                .get()
                .documents
                .map { it.data<RemoteResultFactoryModel>() }
                .first()
            val paymentsTypes = document.paymentsTypes
            Log.i(
                "MdcAppOnly",
                "Firestore --- on fetchPaymentsTypesFactory in firestore : $paymentsTypes"
            )
            paymentsTypes
        } catch (e: Exception) {
            Log.i("MdcAppOnly", "Firestore: on fetchPaymentsTypesFactory $e")
            emptyMap()
        }
    }

    suspend fun fetchPaymentConditionByBrand(brand: String): Map<String, Map<String, Any>> {
        return try {
            val document = factoriesCollection
                .where { FieldPath("Marcas").containsAny(listOf(brand)) }
                .get()
                .documents
                .map { it.data<RemoteResultFactoryModel>() }
                .first()
            val paymentsTypes = document.paymentsTypes
            Log.i(
                "MdcAppOnly",
                "Firestore --- on fetchPaymentsTypesFactory in firestore : $paymentsTypes"
            )
            paymentsTypes
        } catch (e: Exception) {
            Log.i("MdcAppOnly", "Firestore ---- on fetchPaymentsTypesFactory $e")
            emptyMap()
        }

    }

    suspend fun setPaymentsConditionsFactory(
        factoryName: String,
        data: List<PaymentCondition>
    ): Boolean {
        return try {
            factoriesCollection.document(factoryName)
                .update(mapOf("Condiciones" to data))
            true
        } catch (e: Exception) {
            Log.e("MdcAppOnly", "Firestore: on setPaymentsConditionsFactory $e")
            false
        }
    }

    suspend fun addPaymentToRegister(data: RemotePaymentRegisterResult): Boolean {
        return try {
            paymentsRegisterCollection
                .document(data.id.toString())
                .set(data)
            Log.i("MdcAppOnly", "Firestore --- On addPaymentToRegister $data successful")
            true
        } catch (e: Exception) {
            Log.e("MdcAppOnly", "Firestore--- onAddPaymentToRegister $e")
            false
        }
    }

    suspend fun fetchLastIdFromPayments(): Int {
        return try {
            val documents = paymentsRegisterCollection
                .get()
                .documents
                .map { it.data<RemotePaymentRegisterResult>() }
            val list = documents.maxByOrNull { it.id }!!
            if (documents.isEmpty()) 0 else list.id
        } catch (e: Exception) {
            Log.e("MdcAppOnly", "firestore --- on fetchLastIdFromPayments $e")
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
            // Update in global mirror
            allBillingsCollection
                .document(document)
                .update(data)

            // Update in hierarchy
            orderBillingsCollection(clientId, orderId)
                .document(document)
                .update(data)
            
            Log.i("MdcAppOnly", "firestore --- on updateBilling success $data")
            true
        } catch (e: Exception) {
            Log.e("MdcAppOnly", "firestore --- on updateBilling $e")
            false
        }
    }

    suspend fun saveBilling(
        clientId: String,
        orderId: String,
        data: RemoteResultBillingModel
    ): Boolean {
        return try {
            // Save in hierarchy
            orderBillingsCollection(clientId, orderId)
                .document(data.billingNumber)
                .set(data)

            // Save in global collection for Dashboard
            allBillingsCollection
                .document(data.billingNumber)
                .set(data)

            Log.i("MdcAppOnly", "firestore --- on saveBilling success $data")
            true
        } catch (e: Exception) {
            Log.e("MdcAppOnly", "firestore --- on saveBilling $e")
            false
        }
    }

    suspend fun fetchPaymentRegisterByNumberList(documentList: List<String>): List<RemotePaymentRegisterResult> {
        val paymentRegisterResult: MutableList<RemotePaymentRegisterResult> = mutableListOf()
        return try {
            documentList.forEach { numberDocument ->
                val data = paymentsRegisterCollection
                    .where { FieldPath("Remito").equalTo(numberDocument) }
                    .get()
                    .documents
                    .map { it.data<RemotePaymentRegisterResult>() }

                Log.e("firestore", "on fetchPaymentRegisterByNumberList: $data")
                if (data.isNotEmpty())
                    paymentRegisterResult.addAll(data)
            }
            paymentRegisterResult
        } catch (e: Exception) {
            Log.e("firestore", "on fetchPaymentRegisterByNumberList: $e ")
            paymentRegisterResult
        }
    }

    suspend fun fetchFactoriesLisName(): List<String> {
        return try {
            val documents = factoriesCollection
                .get()
                .documents
                .map { it.data<RemoteResultFactoryModel>() }
            val nameList = emptyList<String>().toMutableList()
            documents.forEach { nameList.add(it.name) }
            nameList
        } catch (e: Exception) {
            Log.e("firestore", "on fetchFactoriesLisName: $e ")
            emptyList()
        }
    }

    suspend fun fetchAllFactories(): List<RemoteResultFactoryModel> {
        return try {
            factoriesCollection
                .get()
                .documents
                .map { it.data<RemoteResultFactoryModel>() }
        } catch (e: Exception) {
            Log.e("firestore", "on fetchAllFactories: $e ")
            emptyList()
        }
    }

    suspend fun fetchBillingsByBrand(
        brand: String,
        clientId: String
    ): List<RemoteResultBillingModel> {
        return try {
            val documents = allBillingsCollection
                .where { FieldPath("Cliente Id").equalTo(clientId) }
                .where { FieldPath("Marca").equalTo(brand) }
                .get()
                .documents
                .map { it.data<RemoteResultBillingModel>() }
            Log.i("OrderService", "on fetchBillingsByBrand: $documents")
            documents
        } catch (e: Exception) {
            println("OrderService: on fetchBillingsByBrand $e")
            emptyList()
        }
    }

    suspend fun fetchInvoiceByNumber(invoiceNumber: String): RemoteResultBillingModel {
        return try {
            val document = allBillingsCollection
                .where { FieldPath("Numero").equalTo(invoiceNumber) }
                .get()
                .documents
                .map { it.data<RemoteResultBillingModel>() }
                .first()
            Log.i("OrderService", "on fetchInvoiceByNumber: $document")

            document
        } catch (e: Exception) {
            println("OrderService: on fetchInvoiceByNumber $e")
            RemoteResultBillingModel()
        }
    }

    fun observeInvoiceByNumber(invoiceNumber: String): Flow<RemoteResultBillingModel> {
        return allBillingsCollection
            .where { FieldPath("Numero").equalTo(invoiceNumber) }
            .snapshots()
            .map { snapshot ->
                snapshot.documents.firstOrNull()?.data<RemoteResultBillingModel>()
                    ?: RemoteResultBillingModel()
            }
    }

    fun observeBillingsByClient(clientId: String): Flow<List<RemoteResultBillingModel>> {
        return allBillingsCollection
            .where { FieldPath("Cliente Id").equalTo(clientId) }
            .snapshots()
            .map { snapshot ->
                snapshot.documents.map { it.data<RemoteResultBillingModel>() }
            }
    }

    fun observePaymentsByInvoice(invoiceNumber: String): Flow<List<RemotePaymentRegisterResult>> {
        return paymentsRegisterCollection
            .where { FieldPath("Remito").equalTo(invoiceNumber) }
            .snapshots()
            .map { snapshot ->
                snapshot.documents.map { it.data<RemotePaymentRegisterResult>() }
            }
    }

    fun observeAllBillings(): Flow<List<RemoteResultBillingModel>> {
        return allBillingsCollection
            .snapshots()
            .map { snapshot ->
                snapshot.documents.map { it.data<RemoteResultBillingModel>() }
            }
    }

    fun observeBuyOrdersByClient(clientId: String): Flow<List<RemoteResultBuyOrder>> {
        return clientOrdersCollection(clientId)
            .snapshots()
            .map { snapshot ->
                snapshot.documents.map { it.data<RemoteResultBuyOrder>() }
            }
    }

    fun observeAllOrders(): Flow<List<RemoteResultOrder>> {
        return ordersCollection
            .snapshots()
            .map { snapshot ->
                snapshot.documents.map { it.data<RemoteResultOrder>() }
            }
    }

    fun observeOrdersByFactory(name: String): Flow<List<RemoteResultOrder>> {
        val query = if (name == "all") ordersCollection else ordersCollection.where {
            FieldPath("Marca").equalTo(name)
        }
        return query
            .snapshots()
            .map { snapshot ->
                snapshot.documents.map { it.data<RemoteResultOrder>() }
            }
    }

    suspend fun saveOrder(clientId: String, order: RemoteResultBuyOrder): Boolean {
        return try {
            val nextNumber = getNextOrderNumber()
            val orderId = nextNumber.toString()

            val finalOrder = order.copy(
                id = orderId,
                order = orderId,
                loadedDate = "" // TODO: set date properly
            )

            clientOrdersCollection(clientId).document(orderId).set(finalOrder)
            true
        } catch (e: Exception) {
            Log.e("OrderService", "Error saving order", e)
            false
        }
    }

    private suspend fun getNextOrderNumber(): Int {
        val configDoc = userDoc.collection("config").document("counters")
        return try {
            val snapshot = configDoc.get()
            val current = if (snapshot.exists) snapshot.get<Int>("lastOrderNumber") else 0
            val next = current + 1
            configDoc.set(mapOf("lastOrderNumber" to next))
            next
        } catch (e: Exception) {
            Log.e("OrderService", "Error getting next order number", e)
            1
        }
    }

    suspend fun saveFactory(factory: RemoteResultFactoryModel): Boolean {
        return try {
            factoriesCollection.document(factory.name).set(factory)
            true
        } catch (e: Exception) {
            Log.e("OrderService", "Error saving factory", e)
            false
        }
    }

    suspend fun deleteFactory(factoryName: String): Boolean {
        return try {
            factoriesCollection.document(factoryName).delete()
            true
        } catch (e: Exception) {
            Log.e("OrderService", "Error deleting factory", e)
            false
        }
    }
}
