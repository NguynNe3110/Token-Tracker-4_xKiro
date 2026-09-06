package com.uzuu.adminitratortoken.ui.screens.dashboard

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.uzuu.adminitratortoken.data.local.entity.AccountEntity
import com.uzuu.adminitratortoken.data.repository.TokenRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AccountUsage(
    val account: AccountEntity,
    val usedToday: Long = 0L,
    val limitPerDay: Long = 0L
) {
    val percent: Float
        get() = if (limitPerDay > 0) (usedToday.toFloat() / limitPerDay).coerceIn(0f, 1f) else 0f
    val isOverLimit: Boolean
        get() = limitPerDay > 0 && usedToday > limitPerDay
}

data class DashboardUiState(
    val accountUsages: List<AccountUsage> = emptyList(),
    val isRefreshing: Boolean = false,
    val autoRefreshEnabled: Boolean = false,
    val secondsUntilNextRefresh: Int = 0
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    application: Application,
    private val repository: TokenRepository
) : AndroidViewModel(application) {

    private val prefs = application.getSharedPreferences("dashboard_prefs", Context.MODE_PRIVATE)

    private val _isRefreshing = MutableStateFlow(false)
    private val _autoRefreshEnabled = MutableStateFlow(prefs.getBoolean(KEY_AUTO_REFRESH, false))
    private val _secondsUntilNextRefresh = MutableStateFlow(0)
    private val _usageMap = MutableStateFlow<Map<Long, AccountUsage>>(emptyMap())

    private val _accounts = repository.getAllAccounts()

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState

    private var autoRefreshJob: Job? = null

    init {
        viewModelScope.launch {
            // Combine accounts + latest usage per account + UI state
            combine(
                _accounts,
                _usageMap,
                _isRefreshing,
                _autoRefreshEnabled,
                _secondsUntilNextRefresh
            ) { accounts, usageMap, refreshing, autoOn, countdown ->
                val list = accounts.map { acc ->
                    usageMap[acc.id] ?: AccountUsage(account = acc)
                }
                DashboardUiState(
                    accountUsages = list,
                    isRefreshing = refreshing,
                    autoRefreshEnabled = autoOn,
                    secondsUntilNextRefresh = countdown
                )
            }.collect { _uiState.value = it }
        }
        // Auto-load on init
        refresh()
        // Start auto-refresh loop if previously enabled
        if (_autoRefreshEnabled.value) startAutoRefreshLoop()
    }

    fun toggleAutoRefresh() {
        val newValue = !_autoRefreshEnabled.value
        _autoRefreshEnabled.value = newValue
        prefs.edit().putBoolean(KEY_AUTO_REFRESH, newValue).apply()
        if (newValue) startAutoRefreshLoop() else stopAutoRefreshLoop()
    }

    fun refresh() {
        if (_isRefreshing.value) return
        _isRefreshing.value = true
        viewModelScope.launch {
            val accounts = _accounts.first()
            val newMap = mutableMapOf<Long, AccountUsage>()
            accounts.forEach { account ->
                val result = repository.refreshUsageForAccount(account, 0L, System.currentTimeMillis())
                if (result.isSuccess) {
                    // Read latest record for this account from DB
                    val startOfDay = getStartOfToday()
                    val endOfDay = System.currentTimeMillis()
                    val records = repository.getUsageByAccountAndDateRange(account.id, startOfDay, endOfDay).first()
                    val latest = records.maxByOrNull { it.timestamp }
                    if (latest != null) {
                        newMap[account.id] = AccountUsage(
                            account = account,
                            usedToday = latest.totalTokens.toLong(),
                            limitPerDay = latest.limitPerDay
                        )
                    } else {
                        newMap[account.id] = AccountUsage(account = account)
                    }
                } else {
                    newMap[account.id] = AccountUsage(account = account)
                }
            }
            _usageMap.value = newMap
            _isRefreshing.value = false
        }
    }

    private fun startAutoRefreshLoop() {
        stopAutoRefreshLoop()
        autoRefreshJob = viewModelScope.launch {
            while (_autoRefreshEnabled.value) {
                _secondsUntilNextRefresh.value = REFRESH_INTERVAL_SECONDS
                while (_secondsUntilNextRefresh.value > 0 && _autoRefreshEnabled.value) {
//                    delay(1000)
                    _secondsUntilNextRefresh.value -= 1
                }
                if (_autoRefreshEnabled.value) refresh()
            }
        }
    }

    private fun stopAutoRefreshLoop() {
        autoRefreshJob?.cancel()
        autoRefreshJob = null
        _secondsUntilNextRefresh.value = 0
    }

    override fun onCleared() {
        super.onCleared()
        stopAutoRefreshLoop()
    }

    private fun getStartOfToday(): Long {
        val cal = java.util.Calendar.getInstance()
        cal.set(java.util.Calendar.HOUR_OF_DAY, 0)
        cal.set(java.util.Calendar.MINUTE, 0)
        cal.set(java.util.Calendar.SECOND, 0)
        cal.set(java.util.Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }

    companion object {
        private const val KEY_AUTO_REFRESH = "auto_refresh_enabled"
        private const val REFRESH_INTERVAL_SECONDS = 120
    }
}
