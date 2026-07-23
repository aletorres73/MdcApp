package com.mdcapp.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mdcapp.domain.repositories.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SignUpViewModel(private val authRepository: AuthRepository) : ViewModel() {

    private val _state = MutableStateFlow(UiState())
    val state = _state.asStateFlow()

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
            val user = authRepository.register(email, password)
            if (user != null) {
                _state.update { it.copy(isLoading = false, isSuccess = true) }
            } else {
                _state.update {
                    it.copy(
                        isLoading = false,
                        error = "Error al registrar usuario. Email ya en uso o inválido."
                    )
                }
            }
        }
    }

    fun resetState() {
        _state.update { UiState() }
    }
}
