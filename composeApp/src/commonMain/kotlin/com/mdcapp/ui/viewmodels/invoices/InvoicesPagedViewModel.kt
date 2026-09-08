package com.mdcapp.ui.viewmodels.invoices

import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mdcapp.domain.entities.BillingModel
import com.mdcapp.domain.entities.MovementStatus
import com.mdcapp.domain.entities.UpdateState
import com.mdcapp.domain.entities.recalculate
import com.mdcapp.domain.service.AnalyticsService
import com.mdcapp.domain.usescases.InitConfigUseCase
import com.mdcapp.domain.usescases.invoiceusecase.InvoiceUseCase
import com.mdcapp.ui.utils.AppInstaller
import io.github.aakira.napier.Napier
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@OptIn(FlowPreview::class)
class InvoicesPagedViewModel(
    private val getClients: InvoiceUseCase.GetAllClients,
    private val initConfigUseCase: InitConfigUseCase,
    private val observeBillingsUseCase: InvoiceUseCase.ObserveAllBillings,
    private val updateInvoiceUseCase: InvoiceUseCase.UpdateInvoice,
    private val observeAllPaymentsUseCase: InvoiceUseCase.ObserveAllPayments,
    private val refreshDatabaseUseCase: InvoiceUseCase.RefreshDatabase,
    refreshController: com.mdcapp.domain.service.RefreshController,
    private val analytics: AnalyticsService
) : ViewModel() {

    private val _selectedState = MutableStateFlow("Todas")
    private val _searchQuery = MutableStateFlow(TextFieldValue(""))
    private val _isSearchMode = MutableStateFlow(false)
    private val _isLoading = MutableStateFlow(false)
    private val _message = MutableStateFlow<String?>(null)
    private val _updateState = MutableStateFlow(UpdateState.OK)
    private val _overlay = MutableStateFlow<Overlay>(Overlay.None)

    private val debouncedQuery = _searchQuery
        .map { it.text }
        .distinctUntilChanged()
        .debounce { if (it.isEmpty()) 0 else 300 }

    private data class Status(
        val loading: Boolean,
        val msg: String?,
        val upState: UpdateState,
        val overlay: Overlay,
        val state: String,
        val isSearch: Boolean
    )

    private val status = combine(
        _isLoading, _message, _updateState, _overlay, _selectedState, _isSearchMode
    ) { args ->
        Status(
            loading = args[0] as Boolean,
            msg = args[1] as String?,
            upState = args[2] as UpdateState,
            overlay = args[3] as Overlay,
            state = args[4] as String,
            isSearch = args[5] as Boolean
        )
    }

    val uiState: StateFlow<InvoiceUiState> = combine(
        observeBillingsUseCase(),
        observeAllPaymentsUseCase(),
        _searchQuery,
        debouncedQuery,
        status
    ) { billings, payments, rawQuery, query, status ->
        val pendingInvoices = payments
            .filter { it.status == MovementStatus.PENDIENTE.name }
            .map { it.documentNumber }
            .toSet()

        val filteredBillings = billings.map { it.recalculate() }.filter { b ->
            val matchesState = if (status.state == "Todas") true else b.stateBilling == status.state
            val matchesSearch = if (status.isSearch && query.isNotBlank()) {
                val term = query.lowercase().trim()
                b.clientName.lowercase().contains(term) || b.billingNumber.lowercase()
                    .contains(term)
            } else true

            matchesState && matchesSearch
        }

        InvoiceUiState(
            displayInvoices = filteredBillings.sortedByDescending { it.timeStamp },
            invoicesWithPending = pendingInvoices,
            selectedState = status.state,
            searchQuery = rawQuery,
            isSearchMode = status.isSearch,
            isLoading = status.loading,
            message = status.msg,
            updateState = status.upState,
            overlay = status.overlay,
            availableStates = listOf(
                "Pendiente",
                "Cobrado",
                "Vencido",
                "Por vencer",
                "Devuelta",
                "Cerrada"
            )
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = InvoiceUiState(isLoading = true)
    )

    data class InvoiceUiState(
        val overlay: Overlay = Overlay.None,
        val isLoading: Boolean = false,
        val selectedState: String = "Todas",
        val availableStates: List<String> = emptyList(),
        val searchQuery: TextFieldValue = TextFieldValue(""),
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
        // Escuchar refrescos globales (Botón UI o Foco de ventana)
        refreshController.refreshFlow
            .onEach { refresh() }
            .launchIn(viewModelScope)
    }

    private fun initConfig() {
        viewModelScope.launch {
            val (result, releaseNotes) = initConfigUseCase()
            _updateState.value = result
            _overlay.value = if (result == UpdateState.OK) Overlay.None
            else Overlay.UpdateApp(result, releaseNotes)
            Napier.i("InvoicesPagedViewModel--- initConfig: $result")
        }
    }

    fun onQueryChange(value: String) {
        _searchQuery.value = TextFieldValue(value)
    }

    fun setSearchMode(enabled: Boolean) {
        _isSearchMode.value = enabled
        if (!enabled) {
            _searchQuery.value = TextFieldValue("")
        }
    }

    fun stateSelected(state: String) {
        _selectedState.value = state
    }

    fun refresh() {
        viewModelScope.launch {
            _isLoading.value = true
            refreshDatabaseUseCase()
            kotlinx.coroutines.delay(800) // Feedback visual
            _isLoading.value = false
        }
    }

    fun clearQuery() {
        _searchQuery.value = TextFieldValue("")
    }

    fun closeOverlay() {
        _overlay.value = Overlay.None
    }

    fun updateApk(installer: AppInstaller) {
        viewModelScope.launch {
            val result = initConfigUseCase.download(installer)
            if (!result) {
                _message.value = "Error en el servidor"
            } else {
                if (_updateState.value != UpdateState.FORCE_UPDATE) {
                    _overlay.value = Overlay.None
                }
                _message.value = "Actualización iniciada"
            }
        }
    }

    fun loadNextPage(reset: Boolean = false) {
        // Obsoleto en modo reactivo, pero se mantiene por compatibilidad de firma si la UI lo llama
    }
}
