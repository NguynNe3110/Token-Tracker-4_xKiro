package com.uzuu.adminitratortoken.data.provider

import com.uzuu.adminitratortoken.data.local.entity.UsageRecordEntity

interface TokenProvider {
    val providerType: String
    val displayName: String
    val defaultBaseUrl: String

    suspend fun fetchUsage(
        apiKey: String,
        baseUrl: String,
        accountId: Long,
        startTime: Long,
        endTime: Long
    ): Result<List<UsageRecordEntity>>

    suspend fun validateApiKey(apiKey: String, baseUrl: String): Result<Boolean>
}
