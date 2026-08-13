package com.mdcapp.ui.viewmodels.invoices

import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class InvoicesPagedViewModel(
    private val getClients: InvoiceUseCase.GetAllClients,
    private val initConfigUseCase: InitConfigUseCase,
    private val observeAllBillingsUseCase: InvoiceUseCase.ObserveAllBillings,
    private val updateInvoiceUseCase: InvoiceUseCase.UpdateInvoice,
    private val observeAllPaymentsUseCase: InvoiceUseCase.ObserveAllPayments,
    private val analytics: AnalyticsService
) : ViewModel() {

    private val _uiState = MutableStateFlow(InvoiceUiState())
    val uiState: StateFlow<InvoiceUiState> = _uiState

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
        val stateCounts: Map<String, Int> = emptyMap(),
        val allInvoices: List<BillingModel> = emptyList(),
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
        observeAllBillings()
    }

    private fun observeAllBillings() {
        combine(
            observeAllBillingsUseCase(),
            observeAllPaymentsUseCase()
        ) { billings, payments ->
            val recalculated = billings.map { it.recalculate() }

            // Silent Sync
            recalculated.forEach { billing ->
                val original = billings.find { it.billingNumber == billing.billingNumber }
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

            val pendingInvoices = payments
                .filter { it.status == MovementStatus.PENDIENTE.name }
                .map { it.documentNumber }
                .toSet()

            val counts = recalculated.groupBy { it.stateBilling }
                .mapValues { it.value.size }
                .toMutableMap()

            counts["Todas"] = recalculated.size

            _uiState.update {
                it.copy(
                    stateCounts = counts,
                    allInvoices = recalculated,
                    invoicesWithPending = pendingInvoices
                )
            }

            applyFilters()
        }.launchIn(viewModelScope)
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

    private fun applyFilters() {
        val current = _uiState.value
        val query = current.searchQuery.text.lowercase()
        val state = current.selectedState

        val result = if (current.isSearchMode && query.isNotEmpty()) {
            // Modo Búsqueda Global: Filtra en toda la base por Razón Social O Número (WhatsApp Style)
            current.allInvoices.filter { billing ->
                billing.clientName.lowercase().contains(query) ||
                        billing.billingNumber.lowercase().contains(query)
            }
        } else {
            // Modo Navegación: Filtrar por estado
            if (state == "Todas") {
                current.allInvoices
            } else {
                current.allInvoices.filter { it.stateBilling == state }
            }
        }

        _uiState.update {
            it.copy(displayInvoices = result.sortedByDescending { b -> b.timeStamp })
        }
    }

    fun onQueryChange(value: String) {
        _uiState.update { it.copy(searchQuery = TextFieldValue(value)) }
        applyFilters()
    }

    fun setSearchMode(enabled: Boolean) {
        _uiState.update { it.copy(isSearchMode = enabled) }
        if (!enabled) {
            _uiState.update { it.copy(searchQuery = TextFieldValue("")) }
        }
        applyFilters()
    }

    fun selectSuggestion(value: String) {
        _uiState.update {
            it.copy(
                selectedSuggestion = value,
                searchQuery = TextFieldValue(value)
            )
        }
        applyFilters()
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
        applyFilters()
    }

    fun refresh() {
        // Al ser reactivo, Firestore notificará cualquier cambio automáticamente.
        // No se requiere lógica manual de refresh aquí.
    }

    fun clearQuery() {
        _uiState.update {
            it.copy(
                searchQuery = TextFieldValue("")
            )
        }
        applyFilters()
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

    fun clearMessage() {
        _uiState.value = _uiState.value.copy(message = null)
    }

}

