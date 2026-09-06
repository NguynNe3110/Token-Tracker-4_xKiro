package com.uzuu.adminitratortoken.data.provider.xkiro

import android.util.Log
import com.uzuu.adminitratortoken.data.local.entity.UsageRecordEntity
import com.uzuu.adminitratortoken.data.provider.TokenProvider
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class XkiroTokenProvider @Inject constructor() : TokenProvider {

    override val providerType: String = "XKIRO"
    override val displayName: String = "xKiro"
    override val defaultBaseUrl: String = "https://api.xkiro.com/v1/"

    private fun createService(baseUrl: String): XkiroApiService {
        // Normalize: ensure baseUrl always ends with /v1/ for correct Retrofit resolution
        val normalizedUrl = when {
            baseUrl.endsWith("/v1/") -> baseUrl
            baseUrl.endsWith("/v1") -> "$baseUrl/"
            baseUrl.endsWith("/") -> "${baseUrl}v1/"
            else -> "$baseUrl/v1/"
        }
        Log.d(TAG, "[DEBUG] Using base URL: $normalizedUrl (original: $baseUrl)")

        val client = OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()

        return Retrofit.Builder()
            .baseUrl(normalizedUrl)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(XkiroApiService::class.java)
    }

    override suspend fun validateApiKey(apiKey: String, baseUrl: String): Result<Boolean> {
        Log.d(TAG, "[DEBUG] validateApiKey called")
        return try {
            val service = createService(baseUrl)
            val response = service.getUsage("Bearer $apiKey".trim())
            Log.d(TAG, "[DEBUG] validateApiKey success! plan=${response.plan}, usedToday=${response.freeTokens?.usedToday}")
            Result.success(true)
        } catch (e: Exception) {
            Log.e(TAG, "[DEBUG] validateApiKey failed: ${e.message}", e)
            Result.failure(e)
        }
    }

    override suspend fun fetchUsage(
        apiKey: String,
        baseUrl: String,
        accountId: Long,
        startTime: Long,
        endTime: Long
    ): Result<List<UsageRecordEntity>> {
        Log.d(TAG, "[DEBUG] fetchUsage via GET /v1/usage")
        return try {
            val service = createService(baseUrl)
            // Log raw JSON to verify field names
            val rawResponse = service.getUsageRaw("Bearer $apiKey".trim())
            val rawJson = rawResponse.body()?.string()
            Log.d(TAG, "[DEBUG] Raw JSON response: $rawJson")

            val response = service.getUsage("Bearer $apiKey".trim())
            val usedToday = response.freeTokens?.usedToday ?: 0
            Log.d(TAG, "[DEBUG] Usage data: usedToday=$usedToday, limit=${response.freeTokens?.limitPerDay}, balance=${response.wallet?.balanceUsd}, plan=${response.plan}")

            val limitPerDay = response.freeTokens?.limitPerDay ?: 0L
            val record = UsageRecordEntity(
                accountId = accountId,
                promptTokens = 0,
                completionTokens = 0,
                totalTokens = usedToday.toInt(),
                limitPerDay = limitPerDay,
                model = response.plan ?: "unknown",
                timestamp = System.currentTimeMillis()
            )
            Result.success(listOf(record))
        } catch (e: Exception) {
            Log.e(TAG, "[DEBUG] fetchUsage failed: ${e.javaClass.simpleName}: ${e.message}")
            if (e is retrofit2.HttpException) {
                val errorBody = e.response()?.errorBody()?.string()
                Log.e(TAG, "[DEBUG] HTTP ${e.code()} body: $errorBody")
            }
            Result.failure(e)
        }
    }

    companion object {
        private const val TAG = "DEBUG_XkiroProvider"
    }
}