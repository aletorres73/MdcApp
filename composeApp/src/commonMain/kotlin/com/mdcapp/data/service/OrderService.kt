package com.mdcapp.data.service

import com.mdcapp.data.remote.RemoteBranchOrder
import com.mdcapp.data.remote.RemotePaymentRegisterResult
import com.mdcapp.data.remote.RemoteResultBillingModel
import com.mdcapp.data.remote.RemoteResultBuyOrder
import com.mdcapp.data.remote.RemoteResultFactoryModel
import com.mdcapp.data.remote.RemoteResultOrder
import com.mdcapp.domain.entities.PaymentCondition
import dev.gitlive.firebase.firestore.FieldPath
import dev.gitlive.firebase.firestore.FirebaseFirestore
import io.github.aakira.napier.Napier
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class OrderService(
    private val db: FirebaseFirestore,
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

    private val userDoc
        get() = db.collection("users").document(userId)

    private val ordersCollection get() = userDoc.collection(ORDERS)
    private val factoriesCollection get() = userDoc.collection(FACTORIES)
    private val paymentsRegisterCollection get() = userDoc.collection(PAYMENTS_REGISTER)

    // Global Billings for Dashboard
    private val allBillingsCollection get() = userDoc.collection("allBillings")

    private fun clientOrdersCollection(clientId: String) =
        userDoc.collection("clients").document(clientId).collection(BUY_ORDERS)

    suspend fun fetchAllOrders(): List<RemoteResultOrder> {
// ...
        return try {
            val documents = ordersCollection
                .get()
                .documents
                .map { it.data<RemoteResultOrder>() }
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
            val data = clientOrdersCollection(clientId)
                .where { FieldPath("Orden Id").equalTo(orderId) }
                .get()
                .documents
                .map { it.data<RemoteBranchOrder>() }
            data.first().branch
        } catch (e: Exception) {
            Napier.e("firestore --- on fetchOrderBranchList: $e")
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
            Napier.i("Firestore: on firesotore get orders by factories $e")
            emptyList()
        }
    }

    suspend fun fetchBuyOrder(clientId: String, orderId: String): RemoteResultBuyOrder {
        return try {
            val document = clientOrdersCollection(clientId).document(orderId).get()
            val data = document.data<RemoteResultBuyOrder>()
            val finalData = if (data.id.isEmpty() || data.order.isEmpty()) {
                data.copy(id = document.id, order = document.id)
            } else {
                data
            }
            Napier.i("firestore --- on fetchBuyOrder: $finalData")
            finalData
        } catch (e: Exception) {
            Napier.i("Firestore : on firestore fetchBuyOrder: $e")
            RemoteResultBuyOrder()
        }
    }

    suspend fun fetchBuyOrdersByClient(clientId: String): List<RemoteResultBuyOrder> {
        return try {
            val documents = clientOrdersCollection(clientId)
                .get()
                .documents
                .map { doc ->
                    val data = doc.data<RemoteResultBuyOrder>()
                    if (data.id.isEmpty() || data.order.isEmpty()) {
                        data.copy(id = doc.id, order = doc.id)
                    } else {
                        data
                    }
                }
            documents
        } catch (e: Exception) {
            Napier.e("Error fetchBuyOrdersByClient", e)
            emptyList()
        }
    }

    suspend fun fetchBillings(clientId: String, orderId: String): List<RemoteResultBillingModel> {
        return try {
            val document = allBillingsCollection
                .where { FieldPath("Cliente Id").equalTo(clientId) }
                .where { FieldPath("Orden").equalTo(orderId) }
                .get()
                .documents
                .map { it.data<RemoteResultBillingModel>() }
            Napier.i("firestore --- on fetchBillings: $document")
            document
        } catch (e: Exception) {
            Napier.i("Firestore: on fetchBillings $e")
            emptyList<RemoteResultBillingModel>()
        }
    }

    suspend fun fetchBillingsByClient(clientId: String): List<RemoteResultBillingModel> {
        return try {
            val document = allBillingsCollection
                .where { FieldPath("Cliente Id").equalTo(clientId) }
                .get()
                .documents
                .map { it.data<RemoteResultBillingModel>() }
            Napier.i("OrderService --- on fetchBillingsByClient in firestore : $document")
            document
        } catch (e: Exception) {
            Napier.i("OrderService: on fetchBillingsByClient $e")
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
            Napier.i("Firestore --- on fetchPaymentsTypesFactory in firestore : $paymentsTypes")
            paymentsTypes
        } catch (e: Exception) {
            Napier.i("Firestore: on fetchPaymentsTypesFactory $e")
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
            factoriesCollection.document(factoryName)
                .update(mapOf("Condiciones" to data))
            true
        } catch (e: Exception) {
            Napier.e("Firestore: on setPaymentsConditionsFactory $e")
            false
        }
    }

    suspend fun addPaymentToRegister(data: RemotePaymentRegisterResult): Boolean {
        return try {
            paymentsRegisterCollection
                .document(data.id.toString())
                .set(data)
            Napier.i("Firestore --- On addPaymentToRegister $data successful")
            true
        } catch (e: Exception) {
            Napier.e("Firestore--- onAddPaymentToRegister $e")
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
            // Update in global collection
            allBillingsCollection
                .document(document)
                .update(data)

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
            // Save in global collection
            allBillingsCollection
                .document(data.billingNumber)
                .set(data)

            Napier.i("firestore --- on saveBilling success $data")
            true
        } catch (e: Exception) {
            Napier.e("firestore --- on saveBilling $e")
            false
        }
    }

    suspend fun deleteBilling(invoiceNumber: String): Boolean {
        return try {
            allBillingsCollection.document(invoiceNumber).delete()
            Napier.i("firestore --- on deleteBilling success: $invoiceNumber")
            true
        } catch (e: Exception) {
            Napier.e("firestore --- on deleteBilling $e")
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

                Napier.e("firestore --- on fetchPaymentRegisterByNumberList: $data")
                if (data.isNotEmpty())
                    paymentRegisterResult.addAll(data)
            }
            paymentRegisterResult
        } catch (e: Exception) {
            Napier.e("firestore --- on fetchPaymentRegisterByNumberList: $e ")
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
            Napier.e("firestore --- on fetchFactoriesLisName: $e ")
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
            Napier.e("firestore --- on fetchAllFactories: $e ")
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
            Napier.i("on fetchBillingsByBrand: $documents")
            documents
        } catch (e: Exception) {
            Napier.i("OrderService: on fetchBillingsByBrand $e")
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
            Napier.i("on fetchInvoiceByNumber: $document")

            document
        } catch (e: Exception) {
            Napier.i("OrderService: on fetchInvoiceByNumber $e")
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

    fun observePaymentsByClient(clientId: String): Flow<List<RemotePaymentRegisterResult>> {
        return paymentsRegisterCollection
            .where { FieldPath("Cliente ID").equalTo(clientId) }
            .snapshots()
            .map { snapshot ->
                snapshot.documents.map { it.data<RemotePaymentRegisterResult>() }
            }
    }

    /**
     * SCALABILITY NOTE:
     * Currently observing the entire 'allBillings' collection to support real-time state counts
     * and instant memory search across all invoices.
     * If the collection grows significantly (e.g., > 5000 documents per user), this query should
     * be limited (e.g., .where("Timestamp", ">", oneYearAgo)) to avoid performance degradation
     * and excessive Firestore read costs.
     */
    fun observeAllBillings(): Flow<List<RemoteResultBillingModel>> {
        return allBillingsCollection
            .snapshots()
            .map { snapshot ->
                snapshot.documents.map { it.data<RemoteResultBillingModel>() }
            }
    }

    fun observeAllPayments(): Flow<List<RemotePaymentRegisterResult>> {
        return paymentsRegisterCollection
            .snapshots()
            .map { snapshot ->
                snapshot.documents.map { it.data<RemotePaymentRegisterResult>() }
            }
    }

    fun observeBuyOrdersByClient(clientId: String): Flow<List<RemoteResultBuyOrder>> {
        return clientOrdersCollection(clientId)
            .snapshots()
            .map { snapshot ->
                snapshot.documents.map { doc ->
                    val data = doc.data<RemoteResultBuyOrder>()
                    // Si el ID está vacío por corrupción previa, usar el ID del documento
                    if (data.id.isEmpty() || data.order.isEmpty()) {
                        data.copy(id = doc.id, order = doc.id)
                    } else {
                        data
                    }
                }
            }
    }

    fun observeAllOrders(): Flow<List<RemoteResultOrder>> {
        return ordersCollection
            .snapshots()
            .map { snapshot ->
                snapshot.documents.map { it.data<RemoteResultOrder>() }
            }
    }

    fun observeAllFactories(): Flow<List<RemoteResultFactoryModel>> {
        return factoriesCollection
            .snapshots()
            .map { snapshot ->
                snapshot.documents.map { it.data<RemoteResultFactoryModel>() }
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
                loadedDate = System.currentTimeMillis(),
                timeStamp = System.currentTimeMillis()
            )

            clientOrdersCollection(clientId).document(orderId).set(finalOrder)
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
            // Asegurar que el ID se mantenga correcto durante la actualización
            val finalOrder = order.copy(id = orderId, order = orderId)
            clientOrdersCollection(clientId).document(orderId).set(finalOrder)
            true
        } catch (e: Exception) {
            Napier.e("Error updating order", e)
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
            Napier.e("Error getting next order number", e)
            1
        }
    }

    suspend fun saveFactory(factory: RemoteResultFactoryModel): Boolean {
        return try {
            factoriesCollection.document(factory.name).set(factory)
            true
        } catch (e: Exception) {
            Napier.e("Error saving factory", e)
            false
        }
    }

    suspend fun deleteFactory(factoryName: String): Boolean {
        return try {
            factoriesCollection.document(factoryName).delete()
            true
        } catch (e: Exception) {
            Napier.e("Error deleting factory", e)
            false
        }
    }
}

