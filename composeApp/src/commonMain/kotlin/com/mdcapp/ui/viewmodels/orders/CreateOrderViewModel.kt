package com.mdcapp.ui.viewmodels.orders

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mdcapp.domain.entities.ArticleOrderModel
import com.mdcapp.domain.entities.BuyOrderModel
import com.mdcapp.domain.entities.ClientModel
import com.mdcapp.domain.entities.FactoryModel
import com.mdcapp.domain.entities.PaymentCondition
import com.mdcapp.domain.repositories.OrderRepository
import com.mdcapp.domain.service.AnalyticsService
import com.mdcapp.domain.usescases.clientsusecase.GetClientsUseCase
import com.mdcapp.domain.usescases.ordersusescases.BuyOrderUseCase
import io.github.aakira.napier.Napier
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class CreateOrderViewModel(
    private val saveOrderUseCase: BuyOrderUseCase.SaveOrder,
    private val updateOrderUseCase: BuyOrderUseCase.UpdateOrder,
    private val getBuyOrderByIdUseCase: BuyOrderUseCase.GetBuyOrderById,
    private val repository: OrderRepository,
    private val getClientsUseCase: GetClientsUseCase,
    private val analytics: AnalyticsService
) : ViewModel() {

    private val _state = MutableStateFlow(UiState())
    val state = _state.asStateFlow()

    init {
        analytics.logScreenView("CreateOrder")
    }

    data class UiState(
        val isLoading: Boolean = false,
        val orderId: String? = null,
        val factories: List<FactoryModel> = emptyList(),
        val selectedFactory: FactoryModel? = null,
        val selectedSegment: String = "",
        val selectedCondition: PaymentCondition? = null,
        val clients: List<ClientModel> = emptyList(),
        val selectedClient: ClientModel? = null,
        val articles: List<ArticleOrderModel> = emptyList(),
        val comments: String = "",
        val isSuccess: Boolean = false,
        val error: String? = null,
        // Dialog state
        val dialogArticleName: String = "",
        val dialogArticleColor: String = "",
        val dialogArticlePairs: String = "12",
        val dialogError: String? = null,
        val isDialogSuccess: Boolean = false
    )

    fun initData(clientId: String? = null, orderId: String? = null) {
        observeFactories()
        loadInitialData(clientId, orderId)
    }

    private fun observeFactories() {
        repository.observeFactories()
            .onEach { factories ->
                _state.update { state ->
                    val updatedSelectedFactory =
                        factories.find { it.name == state.selectedFactory?.name }
                    state.copy(
                        factories = factories,
                        selectedFactory = updatedSelectedFactory
                    )
                }
            }
            .launchIn(viewModelScope)
    }

    private fun loadInitialData(clientId: String? = null, orderId: String? = null) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, orderId = orderId) }
            try {
                val clients = getClientsUseCase.getAll()
                var selectedClient =
                    if (clientId != null) clients.find { it.clientId == clientId } else null

                if (orderId != null) {
                    val order = getBuyOrderByIdUseCase(clientId ?: "", orderId)
                    val factory = repository.getFactories().find { it.name == order.factory }
                    if (selectedClient == null) {
                        selectedClient = clients.find { it.clientId == order.clientId }
                    }

                    _state.update {
                        it.copy(
                            selectedClient = selectedClient,
                            selectedFactory = factory,
                            selectedSegment = order.branch,
                            articles = order.articles,
                            comments = order.comments,
                            selectedCondition = factory?.paymentType?.find { it.paymentName == order.paymentCondition }
                        )
                    }
                }

                _state.update {
                    it.copy(
                        isLoading = false,
                        clients = clients,
                        selectedClient = selectedClient
                    )
                }
            } catch (e: Exception) {
                Napier.e("Error loading data for CreateOrder", e)
                _state.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    fun onClientSelected(client: ClientModel) {
        _state.update { it.copy(selectedClient = client) }
    }

    fun onFactorySelected(factory: FactoryModel) {
        _state.update {
            it.copy(
                selectedFactory = factory,
                selectedSegment = "",
                selectedCondition = null
            )
        }
    }

    fun onSegmentSelected(segment: String) {
        _state.update { it.copy(selectedSegment = segment) }
    }

    fun onConditionSelected(condition: PaymentCondition?) {
        _state.update { it.copy(selectedCondition = condition) }
    }

    fun addArticle(name: String, color: String, pairs: Int) {
        val isValid = name.isNotBlank() && color.isNotBlank() && pairs > 0

        if (isValid) {
            val newArticle = ArticleOrderModel(name = name, color = color, pairs = pairs)
            _state.update {
                it.copy(
                    articles = it.articles + newArticle,
                    isDialogSuccess = true,
                    dialogError = null
                )
            }
        } else {
            // Si no es válido, simplemente cerramos y descartamos lo que haya en el diálogo
            _state.update {
                it.copy(
                    isDialogSuccess = true,
                    dialogError = null
                )
            }
        }
    }

    fun addArticleAndContinue() {
        val currentState = _state.value
        val pairs = currentState.dialogArticlePairs.toIntOrNull() ?: 0

        if (currentState.dialogArticleName.isBlank() || currentState.dialogArticleColor.isBlank() || pairs <= 0) {
            _state.update { it.copy(dialogError = "Complete todos los campos del artículo") }
            return
        }

        val newArticle = ArticleOrderModel(
            name = currentState.dialogArticleName,
            color = currentState.dialogArticleColor,
            pairs = pairs
        )
        _state.update {
            it.copy(
                articles = it.articles + newArticle,
                // dialogArticleName = currentState.dialogArticleName, // Keep name for next color
                dialogArticleColor = "",
                dialogArticlePairs = "12",
                dialogError = null
            )
        }
    }

    fun resetDialog() {
        _state.update {
            it.copy(
                dialogArticleName = "",
                dialogArticleColor = "",
                dialogArticlePairs = "12",
                dialogError = null,
                isDialogSuccess = false
            )
        }
    }

    fun onDialogNameChange(name: String) {
        _state.update { it.copy(dialogArticleName = name, dialogError = null) }
    }

    fun onDialogColorChange(color: String) {
        _state.update { it.copy(dialogArticleColor = color, dialogError = null) }
    }

    fun onDialogPairsChange(pairs: String) {
        _state.update { it.copy(dialogArticlePairs = pairs, dialogError = null) }
    }

    fun incrementPairs() {
        val current = _state.value.dialogArticlePairs.toIntOrNull() ?: 0
        _state.update { it.copy(dialogArticlePairs = (current + 12).toString()) }
    }

    fun decrementPairs() {
        val current = _state.value.dialogArticlePairs.toIntOrNull() ?: 0
        val next = if (current >= 12) current - 12 else 0
        _state.update { it.copy(dialogArticlePairs = next.toString()) }
    }

    fun removeArticle(index: Int) {
        _state.update {
            val newList = it.articles.toMutableList()
            newList.removeAt(index)
            it.copy(articles = newList)
        }
    }

    fun onCommentsChange(comments: String) {
        _state.update { it.copy(comments = comments) }
    }

    fun saveOrder() {
        val currentState = _state.value
        if (currentState.selectedClient == null || currentState.selectedFactory == null || currentState.articles.isEmpty()) {
            _state.update { it.copy(error = "Complete todos los campos obligatorios") }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            try {
                val order = BuyOrderModel(
                    id = currentState.orderId ?: "",
                    clientId = currentState.selectedClient.clientId,
                    client = currentState.selectedClient.clientName,
                    factory = currentState.selectedFactory.name,
                    branch = currentState.selectedSegment,
                    articles = currentState.articles,
                    comments = currentState.comments,
                    loadedDate = if (currentState.orderId != null) 0L else System.currentTimeMillis(), // Placeholder, Service handles it
                    order = currentState.orderId ?: "",
                    paymentCondition = currentState.selectedCondition?.paymentName ?: "",
                    discount = currentState.selectedCondition?.discount ?: 0.0,
                    expirationDays = currentState.selectedCondition?.expiration ?: 0,
                    timeStamp = System.currentTimeMillis()
                )

                val success = if (currentState.orderId != null) {
                    updateOrderUseCase(currentState.selectedClient.clientId, order)
                } else {
                    saveOrderUseCase(currentState.selectedClient.clientId, order)
                }

                if (success) {
                    analytics.logEvent(
                        if (currentState.orderId != null) "update_order_success" else "create_order_success",
                        mapOf(
                            "client" to order.client,
                            "factory" to order.factory,
                            "articles_count" to order.articles.size
                        )
                    )
                    _state.update { it.copy(isLoading = false, isSuccess = true) }
                } else {
                    analytics.logEvent(
                        if (currentState.orderId != null) "update_order_failure" else "create_order_failure",
                        mapOf("reason" to "repository_error")
                    )
                    _state.update {
                        it.copy(
                            isLoading = false,
                            error = "Error al guardar el pedido"
                        )
                    }
                }
            } catch (e: Exception) {
                Napier.e("Error saving order", e)
                analytics.logEvent("create_order_error", mapOf("error" to e.message))
                _state.update {
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Error desconocido"
                    )
                }
            }
        }
    }
}

