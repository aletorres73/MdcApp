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

class SignUpViewModel(
    private val authRepository: AuthRepository,
    private val analytics: AnalyticsService
) : ViewModel() {

    private val _state = MutableStateFlow(UiState())
    val state = _state.asStateFlow()

    init {
        analytics.logScreenView("SignUp")
    }

    data class UiState(
        val isLoading: Boolean = false,
        val error: String? = null,
        val isSuccess: Boolean = false
    )

    fun register(email: String, password: String, confirm: String) {
        if (email.isBlank() || password.isBlank()) {
            _state.update { it.copy(error = "Complete todos los campos") }
            return
        }
        if (password != confirm) {
            _state.update { it.copy(error = "Las contraseñas no coinciden") }
            return
        }
        if (password.length < 6) {
            _state.update { it.copy(error = "La contraseña debe tener al menos 6 caracteres") }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            try {
                val user = authRepository.register(email, password)
                if (user != null) {
                    analytics.logEvent("sign_up_success", mapOf("email" to email))
                    _state.update { it.copy(isLoading = false, isSuccess = true) }
                } else {
                    analytics.logEvent(
                        "sign_up_failure",
                        mapOf("email" to email, "reason" to "unknown")
                    )
                    _state.update {
                        it.copy(
                            isLoading = false,
                            error = "Error al registrar usuario. Email ya en uso o inválido."
                        )
                    }
                }
            } catch (e: Exception) {
                Napier.e("SignUp error", e)
                analytics.logEvent("sign_up_error", mapOf("email" to email, "error" to e.message))
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

