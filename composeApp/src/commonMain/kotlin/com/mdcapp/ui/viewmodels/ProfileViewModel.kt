package com.mdcapp.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mdcapp.domain.entities.UserModel
import com.mdcapp.domain.repositories.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ProfileViewModel(private val authRepository: AuthRepository) : ViewModel() {

    private val _state = MutableStateFlow(UiState())
    val state = _state.asStateFlow()

    data class UiState(
        val isLoading: Boolean = false,
        val user: UserModel = UserModel(),
        val isEditing: Boolean = false,
        val successMessage: String? = null,
        val errorMessage: String? = null,
        val showPasswordDialog: Boolean = false
    )

    init {
        loadUserProfile()
    }

    private fun loadUserProfile() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            val profile = authRepository.getUserProfile()
            if (profile != null) {
                _state.update { it.copy(isLoading = false, user = profile) }
            } else {
                _state.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Error al cargar perfil"
                    )
                }
            }
        }
    }

    fun onNameChange(name: String) {
        _state.update { it.copy(user = it.user.copy(name = name)) }
    }

    fun onLastNameChange(lastName: String) {
        _state.update { it.copy(user = it.user.copy(lastName = lastName)) }
    }

    fun toggleEditing() {
        _state.update { it.copy(isEditing = !it.isEditing) }
    }

    fun saveProfile() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            try {
                authRepository.saveUserProfile(_state.value.user)
                _state.update {
                    it.copy(
                        isLoading = false,
                        isEditing = false,
                        successMessage = "Perfil actualizado correctamente"
                    )
                }
            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false, errorMessage = e.message) }
                return@launch
            }
        }
    }

    fun updatePassword(newPassword: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, showPasswordDialog = false) }
            val success = authRepository.updatePassword(newPassword)
            if (success) {
                _state.update {
                    it.copy(
                        isLoading = false,
                        successMessage = "Contraseña actualizada"
                    )
                }
            } else {
                _state.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Error al actualizar contraseña"
                    )
                }
            }
        }
    }

    fun showPasswordDialog() {
        _state.update { it.copy(showPasswordDialog = true) }
    }

    fun hidePasswordDialog() {
        _state.update { it.copy(showPasswordDialog = false) }
    }

    fun clearMessages() {
        _state.update { it.copy(successMessage = null, errorMessage = null) }
    }
}
