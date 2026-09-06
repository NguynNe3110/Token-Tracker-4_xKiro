package com.uzuu.adminitratortoken.ui.screens.account

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.uzuu.adminitratortoken.data.local.entity.AccountEntity
import com.uzuu.adminitratortoken.data.repository.TokenRepository
import com.uzuu.adminitratortoken.data.security.EncryptedStorage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

data class AddAccountUiState(
    val name: String = "",
    val providerType: String = "XKIRO",
    val apiKey: String = "",
    val baseUrl: String = "https://api.xkiro.com/v1/",
    val isValidating: Boolean = false,
    val isSaving: Boolean = false,
    val validationResult: ValidationResult = ValidationResult.Idle,
    val errorMessage: String? = null
)

sealed interface ValidationResult {
    data object Idle : ValidationResult
    data object Validating : ValidationResult
    data object Valid : ValidationResult
    data class Invalid(val message: String) : ValidationResult
}

@HiltViewModel
class AddAccountViewModel @Inject constructor(
    private val repository: TokenRepository,
    private val encryptedStorage: EncryptedStorage
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddAccountUiState())
    val uiState: StateFlow<AddAccountUiState> = _uiState.asStateFlow()

    fun onNameChange(name: String) {
        _uiState.update { it.copy(name = name, errorMessage = null) }
    }

    fun onApiKeyChange(apiKey: String) {
        _uiState.update { it.copy(apiKey = apiKey, errorMessage = null) }
    }

    fun onBaseUrlChange(baseUrl: String) {
        _uiState.update { it.copy(baseUrl = baseUrl, errorMessage = null) }
    }

    fun onProviderChange(providerType: String) {
        // Only XKIRO is supported now
        _uiState.update { it.copy(providerType = "XKIRO", errorMessage = null) }
    }

    fun validateApiKey() {
        val state = _uiState.value
        if (state.apiKey.isBlank()) {
            _uiState.update { it.copy(validationResult = ValidationResult.Invalid("API key is required")) }
            return
        }
        _uiState.update { it.copy(isValidating = true, validationResult = ValidationResult.Validating) }
        viewModelScope.launch {
            val result = repository.validateApiKey(state.providerType, state.apiKey, state.baseUrl)
            result.onSuccess { isValid ->
                _uiState.update {
                    it.copy(
                        isValidating = false,
                        validationResult = if (isValid) ValidationResult.Valid
                        else ValidationResult.Invalid("API key rejected by server")
                    )
                }
            }.onFailure { e ->
                _uiState.update {
                    it.copy(
                        isValidating = false,
                        validationResult = ValidationResult.Invalid(e.message ?: "Validation failed")
                    )
                }
            }
        }
    }

    fun saveAccount(onSuccess: () -> Unit) {
        val state = _uiState.value
        if (state.name.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Name is required") }
            return
        }
        if (state.apiKey.isBlank()) {
            _uiState.update { it.copy(errorMessage = "API key is required") }
            return
        }
        _uiState.update { it.copy(isSaving = true) }
        viewModelScope.launch {
            try {
                val alias = "acc_${System.currentTimeMillis()}_${UUID.randomUUID().toString().take(8)}"
                encryptedStorage.saveApiKey(alias, state.apiKey)
                val account = AccountEntity(
                    name = state.name,
                    providerType = state.providerType,
                    apiKeyAlias = alias,
                    baseUrl = state.baseUrl
                )
                repository.insertAccount(account)
                _uiState.update { it.copy(isSaving = false) }
                onSuccess()
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isSaving = false, errorMessage = e.message ?: "Failed to save account")
                }
            }
        }
    }
}