package com.uzuu.adminitratortoken.ui.screens.account

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.uzuu.adminitratortoken.data.local.entity.AccountEntity
import com.uzuu.adminitratortoken.data.repository.TokenRepository
import com.uzuu.adminitratortoken.data.security.EncryptedStorage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AccountListViewModel @Inject constructor(
    private val repository: TokenRepository,
    private val encryptedStorage: EncryptedStorage
) : ViewModel() {

    val accounts: StateFlow<List<AccountEntity>> = repository.getAllAccounts()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun deleteAccount(account: AccountEntity) {
        viewModelScope.launch {
            encryptedStorage.deleteApiKey(account.apiKeyAlias)
            repository.deleteAccount(account)
        }
    }

    fun refreshAccount(account: AccountEntity) {
        viewModelScope.launch {
            val cal = java.util.Calendar.getInstance()
            cal.add(java.util.Calendar.DAY_OF_YEAR, -7)
            val start = cal.timeInMillis
            val end = System.currentTimeMillis()
            repository.refreshUsageForAccount(account, start, end)
        }
    }

    fun renameAccount(account: AccountEntity, newName: String) {
        val trimmed = newName.trim()
        if (trimmed.isBlank()) return
        viewModelScope.launch {
            repository.updateAccount(account.copy(name = trimmed))
        }
    }
}
