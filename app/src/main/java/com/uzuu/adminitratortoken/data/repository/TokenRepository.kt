package com.uzuu.adminitratortoken.data.repository

import com.uzuu.adminitratortoken.data.local.dao.AccountDao
import com.uzuu.adminitratortoken.data.local.dao.UsageRecordDao
import com.uzuu.adminitratortoken.data.local.entity.AccountEntity
import com.uzuu.adminitratortoken.data.local.entity.UsageRecordEntity
import com.uzuu.adminitratortoken.data.provider.TokenProvider
import com.uzuu.adminitratortoken.data.security.EncryptedStorage
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TokenRepository @Inject constructor(
    private val accountDao: AccountDao,
    private val usageRecordDao: UsageRecordDao,
    private val encryptedStorage: EncryptedStorage,
    private val providers: Map<String, @JvmSuppressWildcards TokenProvider>
) {
    fun getAllAccounts(): Flow<List<AccountEntity>> = accountDao.getAllAccounts()

    suspend fun getAccountById(id: Long): AccountEntity? = accountDao.getAccountById(id)

    suspend fun insertAccount(account: AccountEntity): Long = accountDao.insertAccount(account)

    suspend fun updateAccount(account: AccountEntity) = accountDao.updateAccount(account)

    suspend fun deleteAccount(account: AccountEntity) {
        usageRecordDao.deleteByAccountId(account.id)
        accountDao.deleteAccount(account)
    }

    fun getUsageByDateRange(startTime: Long, endTime: Long): Flow<List<UsageRecordEntity>> =
        usageRecordDao.getUsageByDateRange(startTime, endTime)

    fun getUsageByAccountAndDateRange(
        accountId: Long,
        startTime: Long,
        endTime: Long
    ): Flow<List<UsageRecordEntity>> =
        usageRecordDao.getUsageByAccountAndDateRange(accountId, startTime, endTime)

    fun getTotalTokensInRange(startTime: Long, endTime: Long): Flow<Int?> =
        usageRecordDao.getTotalTokensInRange(startTime, endTime)

    suspend fun refreshUsageForAccount(account: AccountEntity, startTime: Long, endTime: Long): Result<Unit> {
        val provider = providers[account.providerType]
            ?: return Result.failure(IllegalArgumentException("Unknown provider: ${account.providerType}"))
        val apiKey = encryptedStorage.getApiKey(account.apiKeyAlias)
            ?: return Result.failure(IllegalStateException("API key not found for account: ${account.name}"))
        val result = provider.fetchUsage(apiKey, account.baseUrl, account.id, startTime, endTime)
        result.onSuccess { records ->
            if (records.isNotEmpty()) {
                usageRecordDao.insertAll(records)
            }
        }
        return result.map { }
    }

    suspend fun validateApiKey(providerType: String, apiKey: String, baseUrl: String): Result<Boolean> {
        val provider = providers[providerType]
            ?: return Result.failure(IllegalArgumentException("Unknown provider: $providerType"))
        return provider.validateApiKey(apiKey, baseUrl)
    }

    fun getAvailableProviders(): List<TokenProvider> = providers.values.toList()
}
