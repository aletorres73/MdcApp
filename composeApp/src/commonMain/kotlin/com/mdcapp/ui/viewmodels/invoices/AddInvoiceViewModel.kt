package com.mdcapp.ui.viewmodels.invoices

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mdcapp.domain.entities.BillingComments
import com.mdcapp.domain.entities.BillingModel
import com.mdcapp.domain.entities.BuyOrderModel
import com.mdcapp.domain.entities.FactoryModel
import com.mdcapp.domain.entities.PaymentCondition
import com.mdcapp.domain.entities.recalculate
import com.mdcapp.domain.logic.SharedDocument
import com.mdcapp.domain.logic.SharedDocumentHandler
import com.mdcapp.domain.repositories.IInvoiceScannerService
import com.mdcapp.domain.repositories.IStorageRepository
import com.mdcapp.domain.service.AnalyticsService
import com.mdcapp.domain.usescases.invoiceusecase.InvoiceUseCase
import com.mdcapp.domain.usescases.ordersusescases.BuyOrderUseCase
import io.github.aakira.napier.Napier
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AddInvoiceViewModel(
    orderId: String,
    private val createInvoiceUseCase: InvoiceUseCase.CreateInvoice,
    private val getBuyOrderUseCase: BuyOrderUseCase.GetBuyOrderById,
    private val getPaymentConditionUseCase: InvoiceUseCase.GetPaymentCondition,
    private val getClientNameUseCase: InvoiceUseCase.GetClientName,
    private val checkInvoiceUseCase: InvoiceUseCase.GetInvoiceByNumber,
    private val repository: com.mdcapp.domain.repositories.OrderRepository,
    private val analytics: AnalyticsService,
    private val scannerService: IInvoiceScannerService,
    private val storageRepository: IStorageRepository,
    private val observeBuyOrdersByClientUseCase: BuyOrderUseCase.ObserveBuyOrdersByClient
) : ViewModel() {

    private val _state = MutableStateFlow(UiState(orderId = orderId))
    val state = _state.asStateFlow()

    init {
        analytics.logScreenView("AddInvoice")
        collectPendingDocument()
    }

    private fun collectPendingDocument() {
        viewModelScope.launch {
            SharedDocumentHandler.pendingDocument.collect { doc ->
                if (doc != null) {
                    _state.update { it.copy(pendingDocument = doc, scanningDocument = true) }
                    SharedDocumentHandler.clearPendingDocument()
                    try {
                        val result = scannerService.scanInvoice(doc.fileBytes, doc.mimeType)
                        _state.update {
                            it.copy(
                                scanningDocument = false,
                                scannedDocumentResult = result
                            )
                        }
                    } catch (e: Exception) {
                        Napier.e("Error scanning document", e)
                        _state.update { it.copy(scanningDocument = false, error = e.message) }
                    }
                }
            }
        }
    }

    data class UiState(
        val orderId: String = "",
        val isLoading: Boolean = false,
        val buyOrder: BuyOrderModel = BuyOrderModel(),
        val paymentConditionList: List<PaymentCondition> = emptyList(),
        val selectedCondition: PaymentCondition? = null,
        val factories: List<FactoryModel> = emptyList(),
        val selectedFactory: FactoryModel? = null,
        val branches: List<String> = emptyList(),
        val selectedBranch: String = "",
        val isSuccess: Boolean = false,
        val isExistingInvoice: Boolean = false,
        val error: String? = null,
        val scanningDocument: Boolean = false,
        val scannedDocumentResult: Map<String, String>? = null,
        val pendingDocument: SharedDocument? = null,
        val clientOrders: List<BuyOrderModel> = emptyList()
    )

    fun onNumberChange(number: String) {
        if (number.isBlank()) {
            _state.update { it.copy(isExistingInvoice = false) }
            return
        }

        viewModelScope.launch {
            try {
                val existing = checkInvoiceUseCase(number)
                _state.update { it.copy(isExistingInvoice = existing.billingNumber.isNotEmpty()) }
            } catch (e: Exception) {
                _state.update { it.copy(isExistingInvoice = false) }
            }
        }
    }

    fun loadData(clientId: String, orderId: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            try {
                // Observar pedidos del cliente
                if (clientId.isNotBlank()) {
                    viewModelScope.launch {
                        observeBuyOrdersByClientUseCase(clientId).collect { orders ->
                            _state.update { it.copy(clientOrders = orders) }
                        }
                    }
                }

                val factories = repository.getFactories()
                if (orderId.isNotBlank()) {
                    val buyOrder = getBuyOrderUseCase(clientId, orderId)
                    val conditions = getPaymentConditionUseCase(buyOrder.branch, buyOrder.factory)

                    val inheritedCondition = if (buyOrder.paymentCondition.isNotEmpty()) {
                        PaymentCondition(
                            paymentName = buyOrder.paymentCondition,
                            discount = buyOrder.discount,
                            expiration = buyOrder.expirationDays
                        )
                    } else null

                    _state.update {
                        it.copy(
                            isLoading = false,
                            buyOrder = buyOrder,
                            paymentConditionList = conditions,
                            selectedCondition = inheritedCondition,
                            selectedFactory = factories.find { f -> f.name == buyOrder.factory },
                            selectedBranch = buyOrder.branch,
                            factories = factories
                        )
                    }
                } else {
                    val client = getClientNameUseCase(clientId)
                    _state.update {
                        it.copy(
                            isLoading = false,
                            buyOrder = BuyOrderModel(
                                clientId = clientId,
                                client = client.clientName
                            ),
                            factories = factories
                        )
                    }
                }
            } catch (e: Exception) {
                Napier.e("Error loading data for AddInvoice", e)
                _state.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    fun selectOrder(order: BuyOrderModel?) {
        viewModelScope.launch {
            if (order == null) {
                _state.update {
                    it.copy(
                        orderId = "",
                        buyOrder = BuyOrderModel(
                            clientId = it.buyOrder.clientId,
                            client = it.buyOrder.client
                        )
                    )
                }
            } else {
                val conditions = getPaymentConditionUseCase(order.branch, order.factory)
                val inheritedCondition = if (order.paymentCondition.isNotEmpty()) {
                    PaymentCondition(
                        paymentName = order.paymentCondition,
                        discount = order.discount,
                        expiration = order.expirationDays
                    )
                } else null

                val matchedFactory = _state.value.factories.find { f -> f.name == order.factory }

                _state.update {
                    it.copy(
                        orderId = order.id,
                        buyOrder = order,
                        paymentConditionList = conditions,
                        selectedCondition = inheritedCondition,
                        selectedFactory = matchedFactory,
                        selectedBranch = order.branch
                    )
                }
            }
        }
    }

    fun onFactorySelected(factory: FactoryModel) {
        viewModelScope.launch {
            _state.update {
                it.copy(
                    selectedFactory = factory,
                    selectedBranch = "",
                    isLoading = true
                )
            }
            try {
                val conditions = getPaymentConditionUseCase("", factory.name)
                _state.update { it.copy(paymentConditionList = conditions, isLoading = false) }
            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false) }
            }
        }
    }

    fun onBranchSelected(branch: String) {
        _state.update { it.copy(selectedBranch = branch) }
    }

    fun onScannedDocumentResultConsumed() {
        _state.update { it.copy(scannedDocumentResult = null) }
    }

    fun saveInvoice(
        number: String,
        amount: Double,
        condition: PaymentCondition?,
        issueDate: Long,
        deliveryDate: Long,
        payDate: Long,
        type: String,
        notes: String
    ) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            try {
                val now = System.currentTimeMillis()

                val comments = if (notes.isNotBlank()) {
                    listOf(BillingComments(notes, now))
                } else emptyList()

                val baseInvoice = BillingModel(
                    billingNumber = number,
                    orderId = _state.value.orderId,
                    total = amount,
                    brand = _state.value.selectedFactory?.name ?: "",
                    branch = _state.value.selectedBranch,
                    clientId = _state.value.buyOrder.clientId,
                    clientName = _state.value.buyOrder.client,
                    payDate = payDate,
                    deliveryDate = deliveryDate,
                    paymentCondition = condition?.paymentName ?: "",
                    expectedDiscount = condition?.discount ?: 0.0,
                    type = type,
                    comments = comments,
                    stateBilling = "Pendiente",
                    loadDate = if (issueDate != 0L) issueDate else now,
                    timeStamp = System.currentTimeMillis()
                )

                val finalInvoice =
                    if (condition != null) baseInvoice.recalculate(condition) else baseInvoice

                var finalInvoiceToSave = finalInvoice
                val pendingDoc = _state.value.pendingDocument
                if (pendingDoc != null) {
                    val extension = pendingDoc.fileName.substringAfterLast('.', "")
                    val extPath = if (extension.isNotEmpty()) ".$extension" else ""
                    val path = "invoices/${_state.value.buyOrder.client}/${number}_${now}$extPath"
                    try {
                        val url = storageRepository.uploadFile(path, pendingDoc.fileBytes)
                        finalInvoiceToSave = finalInvoice.copy(documentUrl = url)
                    } catch (e: Exception) {
                        Napier.e(
                            "Error uploading document to Firebase Storage (Check Storage Rules)",
                            e
                        )
                    }
                    SharedDocumentHandler.clearPendingDocument()
                }

                val success = createInvoiceUseCase(
                    _state.value.buyOrder.clientId,
                    _state.value.orderId,
                    finalInvoiceToSave
                )
                if (success) {
                    analytics.logEvent(
                        "add_invoice_success", mapOf(
                            "invoice_number" to number,
                            "amount" to amount,
                            "client" to finalInvoice.clientName
                        )
                    )
                    _state.update { it.copy(isLoading = false, isSuccess = true) }
                } else {
                    analytics.logEvent("add_invoice_failure", mapOf("reason" to "repository_error"))
                    _state.update { it.copy(isLoading = false, error = "Error al guardar factura") }
                }
            } catch (e: Exception) {
                Napier.e("Error saving invoice", e)
                analytics.logEvent("add_invoice_error", mapOf("error" to e.message))
                _state.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }
}

