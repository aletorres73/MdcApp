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

class OrderService(
    private val db: FirebaseFirestore
) {
    companion object {
        const val ORDERS = "Orders"
        const val BUY_ORDERS = "buyOrders"
        const val BILLINGS = "billings"
        const val FACTORIES = "factories"
        const val PAYMENTS_REGISTER = "paymentRegister"
    }

    suspend fun fetchAllOrders(): List<RemoteResultOrder> {
        return try {
            val documents = db.collection(ORDERS)
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

    suspend fun fetchOrderBranch(orderId: String): String {
        return try {
            val data = db.collection(BUY_ORDERS)
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
            val document = db.collection(ORDERS)
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

    suspend fun fetchBuyOrder(orderId: String): RemoteResultBuyOrder {
        return try {
            val document = db.collection(BUY_ORDERS)
                .where { FieldPath("Orden Id").equalTo(orderId) }
                .get()
                .documents
                .map { it.data<RemoteResultBuyOrder>() }
                .first()
            Log.i("MdcAppOnly", "firestore --- on fetchBuyOrder in firestore: $document")
            document
        } catch (e: Exception) {
            Log.i("MdcAppOnly", "Firestore : on firestore fetchBuyOrder: $e")
            RemoteResultBuyOrder()
        }
    }

    suspend fun fetchBillings(orderId: String): List<RemoteResultBillingModel> {
        return try {
            val document = db.collection(BILLINGS)
                .where { FieldPath("Orden").equalTo(orderId) }
                .get()
                .documents
                .map { it.data<RemoteResultBillingModel>() }
            Log.i("MdcAppOnly", "firestore --- on fetchBillings in firestore : $document")
            document
        } catch (e: Exception) {
            Log.i("MdcAppOnly", "Firestore: on fetchBillings $e")
            emptyList()
        }
    }

    suspend fun fetchBillingsByClient(clientId: String): List<RemoteResultBillingModel> {
        return try {
            val document = db.collection(BILLINGS)
                .where { FieldPath("Cliente id").equalTo(clientId) }
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
            val document = db.collection(FACTORIES)
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
            val document = db.collection(FACTORIES)
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
            db.collection(FACTORIES).document(factoryName)
                .update(mapOf("Condiciones" to data))
            true
        } catch (e: Exception) {
            Log.e("MdcAppOnly", "Firestore: on setPaymentsConditionsFactory $e")
            false
        }
    }

    suspend fun addPaymentToRegister(data: RemotePaymentRegisterResult): Boolean {
        return try {
            db.collection(PAYMENTS_REGISTER)
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
            val documents = db.collection(PAYMENTS_REGISTER)
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

    suspend fun updateBilling(document: String, data: RemoteResultBillingModel): Boolean {
        return try {
            db.collection(BILLINGS)
                .document(document)
                .update(data)
            Log.i("MdcAppOnly", "firestore --- on updateBilling success $data")
            true
        } catch (e: Exception) {
            Log.e("MdcAppOnly", "firestore --- on updateBilling $e")
            false
        }

    }

    suspend fun fetchPaymentRegisterByNumberList(documentList: List<String>): List<RemotePaymentRegisterResult> {
        val paymentRegisterResult: MutableList<RemotePaymentRegisterResult> = mutableListOf()
        return try {
            documentList.forEach { numberDocument ->
                val data = db.collection(PAYMENTS_REGISTER)
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
            val documents = db.collection(FACTORIES)
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

    suspend fun fetchBillingsByBrand(
        brand: String,
        clientId: String
    ): List<RemoteResultBillingModel> {
        return try {
            val documents = db.collection(BILLINGS)
                .where { FieldPath("Cliente id").equalTo(clientId) }
                .where { FieldPath("Marca").equalTo(brand) }
                .get()
                .documents
                .map { it.data<RemoteResultBillingModel>() }
            Log.i("OrderService", "on fetchBillingsByClient in firestore : $documents")
            documents
        } catch (e: Exception) {
            println("OrderService: on fetchBillingsByClient $e")
            emptyList()
        }
    }

    suspend fun fetchInvoiceByNumber(invoiceNumber: String): RemoteResultBillingModel {
        return try {
            val document = db.collection(BILLINGS)
                .where { FieldPath("Numero").equalTo(invoiceNumber) }
                .get()
                .documents
                .map { it.data<RemoteResultBillingModel>() }
                .first()
            Log.i("OrderService", "on fetchInvoiceByNumber in firestore : $document")

            document
        } catch (e: Exception) {
            println("OrderService: on fetchInvoiceByNumber $e")
            RemoteResultBillingModel()
        }

    }
}
