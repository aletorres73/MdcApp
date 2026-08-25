package com.mdcapp.ui.viewmodels.invoices

import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mdcapp.data.remote.toBillingDomain
import com.mdcapp.domain.entities.BillingModel
import com.mdcapp.domain.entities.ClientModel
import com.mdcapp.domain.entities.MovementStatus
import com.mdcapp.domain.entities.TypeSearch
import com.mdcapp.domain.entities.UpdateState
import com.mdcapp.domain.entities.recalculate
import com.mdcapp.domain.service.AnalyticsService
import com.mdcapp.domain.usescases.InitConfigUseCase
import com.mdcapp.domain.usescases.invoiceusecase.InvoiceUseCase
import com.mdcapp.ui.utils.AppInstaller
import io.github.aakira.napier.Napier
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

class InvoicesPagedViewModel(
    private val getClients: InvoiceUseCase.GetAllClients,
    private val initConfigUseCase: InitConfigUseCase,
    private val getInvoicePaged: InvoiceUseCase.GetInvoicePaged,
    private val updateInvoiceUseCase: InvoiceUseCase.UpdateInvoice,
    private val observeAllPaymentsUseCase: InvoiceUseCase.ObserveAllPayments,
    private val refreshDatabaseUseCase: InvoiceUseCase.RefreshDatabase,
    refreshController: com.mdcapp.domain.service.RefreshController,
    analytics: AnalyticsService
) : ViewModel() {

    private val _uiState = MutableStateFlow(InvoiceUiState())
    val uiState: StateFlow<InvoiceUiState> = _uiState

    private var searchJob: Job? = null

    data class InvoiceUiState(
        val overlay: Overlay = Overlay.None,
        val invoices: List<BillingModel> = emptyList(),
        val clientNameList: List<ClientModel> = emptyList(),
        val isLoading: Boolean = false,
        val selectedState: String = "Todas",
        val cursor: String? = null,
        val endReached: Boolean = false,
        val availableStates: List<String> = listOf(
            "Todas",
            "Pendiente",
            "Cobrado",
            "Vencido",
            "Por vencer",
            "Devuelta",
            "Cerrada"
        ),
        val clientSearch: String? = null,
        val numberSearch: String? = null,
        val searchQuery: TextFieldValue = TextFieldValue(""),
        val typeSearch: TypeSearch? = TypeSearch.Client,
        val selectedSuggestion: String? = null,
        val updateState: UpdateState = UpdateState.OK,
        val message: String? = null,
        val invoicesWithPending: Set<String> = emptySet(),
        val displayInvoices: List<BillingModel> = emptyList(),
        val isSearchMode: Boolean = false
    )

    sealed interface Overlay {
        data object None : Overlay
        data class UpdateApp(val state: UpdateState, val releasesNotes: String) : Overlay
    }

    init {
        analytics.logScreenView("InvoicesPaged")
        initConfig()
        loadAllClients()
        observePayments()
        loadNextPage(reset = true)
        // Escuchar refrescos globales (Botón UI o Foco de ventana)
        refreshController.refreshFlow
            .onEach { refresh() }
            .launchIn(viewModelScope)
    }

    private fun observePayments() {
        observeAllPaymentsUseCase()
            .onEach { payments ->
                val pendingInvoices = payments
                    .filter { it.status == MovementStatus.PENDIENTE.name }
                    .map { it.documentNumber }
                    .toSet()
                _uiState.update { it.copy(invoicesWithPending = pendingInvoices) }
            }.launchIn(viewModelScope)
    }

    fun loadNextPage(reset: Boolean = false) {
        if (_uiState.value.isLoading || (_uiState.value.endReached && !reset)) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val current = _uiState.value
                val cursor = if (reset) null else current.cursor

                val query = current.searchQuery.text
                // Consideramos número solo si es puramente digital o tiene guiones/barras
                val isNumber =
                    query.isNotEmpty() && query.all { it.isDigit() || it == '-' || it == '/' }

                val (page, nextCursor) = getInvoicePaged.loadNextPage(
                    limit = 20,
                    state = current.selectedState,
                    cursor = cursor,
                    client = if (current.isSearchMode && query.isNotEmpty() && !isNumber) query else null,
                    number = if (current.isSearchMode && query.isNotEmpty() && isNumber) query else null
                )

                var items = page.items

                // Si no hay resultados y es búsqueda por nombre, intentamos con Capitalización
                if (items.isEmpty() && current.isSearchMode && query.isNotEmpty() && !isNumber) {
                    val capitalizedQuery = query.lowercase().replaceFirstChar { it.uppercase() }
                    if (capitalizedQuery != query) {
                        val (secondPage, _) = getInvoicePaged.loadNextPage(
                            limit = 20,
                            state = current.selectedState,
                            cursor = null,
                            client = capitalizedQuery
                        )
                        items = secondPage.items
                    }
                }

                val newItems = items.map { it.toBillingDomain().recalculate() }

                // Silent Sync for visible items
                newItems.forEach { billing ->
                    val original = page.items.find { it.billingNumber == billing.billingNumber }
                    if (original != null && original.stateBilling != billing.stateBilling) {
                        viewModelScope.launch {
                            updateInvoiceUseCase(
                                billing.clientId,
                                billing.orderId,
                                billing.billingNumber,
                                billing
                            )
                        }
                    }
                }

                _uiState.update {
                    val updatedInvoices = if (reset) newItems else (it.displayInvoices + newItems)
                    // Asegurar que no haya duplicados por número de factura
                    val distinctInvoices = updatedInvoices.distinctBy { b -> b.billingNumber }

                    it.copy(
                        displayInvoices = distinctInvoices.sortedByDescending { b -> b.timeStamp },
                        cursor = nextCursor,
                        endReached = page.endReached,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                Napier.e("Error loading invoices page", e)
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    private fun initConfig() {
        viewModelScope.launch {
            val (result, releaseNotes) = initConfigUseCase()
            _uiState.update {
                it.copy(
                    updateState = result,
                    overlay = if (result == UpdateState.OK) Overlay.None
                    else Overlay.UpdateApp(result, releaseNotes)
                )
            }
            Napier.i("InvoicesPagedViewModel--- initConfig: $result")
        }
    }

    fun onQueryChange(value: String) {
        _uiState.update { it.copy(searchQuery = TextFieldValue(value)) }
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(500.milliseconds)
            loadNextPage(reset = true)
        }
    }

    fun setSearchMode(enabled: Boolean) {
        _uiState.update { it.copy(isSearchMode = enabled) }
        if (!enabled) {
            _uiState.update { it.copy(searchQuery = TextFieldValue("")) }
        }
        loadNextPage(reset = true)
    }

    private fun loadAllClients() {
        viewModelScope.launch {
            _uiState.update { it.copy(clientNameList = getClients()) }
        }
    }

    fun stateSelected(state: String) {
        _uiState.update {
            it.copy(selectedState = state)
        }
        loadNextPage(reset = true)
    }

    fun refresh() {
        refreshDatabaseUseCase()
        loadNextPage(reset = true)
    }

    fun clearQuery() {
        _uiState.update {
            it.copy(
                searchQuery = TextFieldValue("")
            )
        }
        loadNextPage(reset = true)
    }

    fun closeOverlay() {
        _uiState.value = _uiState.value.copy(overlay = Overlay.None)
    }

    fun updateApk(installer: AppInstaller) {
        viewModelScope.launch {
            Napier.i("updating apk")
            val result = initConfigUseCase.download(installer)
            if (!result) {
                _uiState.update {
                    it.copy(message = "Error en el servidor")
                }
            } else {
                _uiState.update {
                    val nextOverlay = if (it.updateState == UpdateState.FORCE_UPDATE) {
                        it.overlay // Mantener el diálogo si es forzado
                    } else {
                        Overlay.None // Cerrar si es opcional
                    }
                    it.copy(
                        message = "Actualización iniciada",
                        overlay = nextOverlay
                    )
                }
            }
        }
    }

}

