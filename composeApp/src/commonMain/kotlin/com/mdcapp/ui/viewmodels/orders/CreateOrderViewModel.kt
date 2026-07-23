package com.mdcapp.ui.viewmodels.orders

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mdcapp.data.model.ArticleOrderModel
import com.mdcapp.data.model.BuyOrderModel
import com.mdcapp.data.model.ClientModel
import com.mdcapp.domain.usescases.clientsusecase.GetClientsUseCase
import com.mdcapp.domain.usescases.ordersusescases.BuyOrderUseCase
import com.mdcapp.domain.usescases.ordersusescases.GetFactoriesListUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class CreateOrderViewModel(
    private val saveOrderUseCase: BuyOrderUseCase.SaveOrder,
    private val getFactoriesUseCase: GetFactoriesListUseCase,
    private val getClientsUseCase: GetClientsUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(UiState())
    val state = _state.asStateFlow()

    data class UiState(
        val isLoading: Boolean = false,
        val factories: List<String> = emptyList(),
        val clients: List<ClientModel> = emptyList(),
        val selectedClient: ClientModel? = null,
        val selectedFactory: String = "",
        val articles: List<ArticleOrderModel> = emptyList(),
        val comments: String = "",
        val isSuccess: Boolean = false,
        val error: String? = null
    )

    init {
        loadInitialData()
    }

    private fun loadInitialData() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            try {
                val factories = getFactoriesUseCase()
                val clients = getClientsUseCase.getAll()
                _state.update {
                    it.copy(
                        isLoading = false,
                        factories = factories,
                        clients = clients
                    )
                }
            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    fun onClientSelected(client: ClientModel) {
        _state.update { it.copy(selectedClient = client) }
    }

    fun onFactorySelected(factory: String) {
        _state.update { it.copy(selectedFactory = factory) }
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
        if (currentState.selectedClient == null || currentState.selectedFactory.isEmpty() || currentState.articles.isEmpty()) {
            _state.update { it.copy(error = "Complete todos los campos obligatorios y agregue al menos un artículo") }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            val order = BuyOrderModel(
                clientId = currentState.selectedClient.clientId,
                client = currentState.selectedClient.clientName,
                branch = currentState.selectedFactory,
                articles = currentState.articles,
                comments = currentState.comments,
                loadedDate = "" // Se puede setear en el repository o usar un helper
            )
            val success = saveOrderUseCase(order)
            if (success) {
                _state.update { it.copy(isLoading = false, isSuccess = true) }
            } else {
                _state.update { it.copy(isLoading = false, error = "Error al guardar el pedido") }
            }
        }
    }

    fun resetState() {
        _state.update { UiState() }
        loadInitialData()
    }
}
