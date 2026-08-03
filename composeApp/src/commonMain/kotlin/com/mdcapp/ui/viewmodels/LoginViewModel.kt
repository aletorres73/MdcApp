package com.mdcapp.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mdcapp.domain.repositories.AuthRepository
import com.mdcapp.domain.service.AnalyticsService
import io.github.aakira.napier.Napier
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class LoginViewModel(
    private val authRepository: AuthRepository,
    private val analytics: AnalyticsService
) : ViewModel() {

    private val _state = MutableStateFlow(UiState())
    val state = _state.asStateFlow()

    init {
        analytics.logScreenView("Login")
    }

    data class UiState(
        val isLoading: Boolean = false,
        val error: String? = null,
        val isSuccess: Boolean = false
    )

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            try {
                val user = authRepository.login(email, password)
                if (user != null) {
                    analytics.logEvent("login_success", mapOf("email" to email))
                    _state.update { it.copy(isLoading = false, isSuccess = true) }
                } else {
                    analytics.logEvent(
                        "login_failure",
                        mapOf("email" to email, "reason" to "invalid_credentials")
                    )
                    _state.update { it.copy(isLoading = false, error = "Credenciales inválidas") }
                }
            } catch (e: Exception) {
                Napier.e("Login error", e)
                analytics.logEvent("login_error", mapOf("email" to email, "error" to e.message))
                _state.update {
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Error desconocido"
                    )
                }
            }
        }
    }

    fun resetState() {
        _state.update { UiState() }
    }
}

