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
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class CreateOrderViewModel(
    private val saveOrderUseCase: BuyOrderUseCase.SaveOrder,
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
        val factories: List<FactoryModel> = emptyList(),
        val selectedFactory: FactoryModel? = null,
        val selectedSegment: String = "",
        val selectedCondition: PaymentCondition? = null,
        val clients: List<ClientModel> = emptyList(),
        val selectedClient: ClientModel? = null,
        val articles: List<ArticleOrderModel> = emptyList(),
        val comments: String = "",
        val isSuccess: Boolean = false,
        val error: String? = null
    )

    fun initData(clientId: String? = null) {
        loadInitialData(clientId)
    }

    private fun loadInitialData(clientId: String? = null) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            try {
                val factories = repository.getFactories()
                val clients = getClientsUseCase.getAll()
                val selectedClient =
                    if (clientId != null) clients.find { it.clientId == clientId } else null

                _state.update {
                    it.copy(
                        isLoading = false,
                        factories = factories,
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
        val newArticle = ArticleOrderModel(name = name, color = color, pairs = pairs)
        _state.update { it.copy(articles = it.articles + newArticle) }
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
                    clientId = currentState.selectedClient.clientId,
                    client = currentState.selectedClient.clientName,
                    factory = currentState.selectedFactory.name,
                    branch = currentState.selectedSegment,
                    articles = currentState.articles,
                    comments = currentState.comments,
                    loadedDate = System.currentTimeMillis(),
                    order = "",
                    paymentCondition = currentState.selectedCondition?.paymentName ?: "",
                    discount = currentState.selectedCondition?.discount ?: 0.0,
                    expirationDays = currentState.selectedCondition?.expiration ?: 0,
                    timeStamp = System.currentTimeMillis()
                )
                val success = saveOrderUseCase(currentState.selectedClient.clientId, order)
                if (success) {
                    analytics.logEvent(
                        "create_order_success", mapOf(
                            "client" to order.client,
                            "factory" to order.factory,
                            "articles_count" to order.articles.size
                        )
                    )
                    _state.update { it.copy(isLoading = false, isSuccess = true) }
                } else {
                    analytics.logEvent(
                        "create_order_failure",
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

