package com.uzuu.adminitratortoken.data.provider.xkiro

import com.google.gson.annotations.SerializedName

data class UsageResponse(
    val plan: String? = null,
    @SerializedName("free_tokens")
    val freeTokens: FreeTokens? = null,
    val wallet: Wallet? = null
)

data class FreeTokens(
    @SerializedName("used_today")
    val usedToday: Long = 0,
    @SerializedName("limit_per_day")
    val limitPerDay: Long = 0,
    val remaining: Long = 0
)

data class Wallet(
    @SerializedName("balance_usd")
    val balanceUsd: String = "0.000000",
    @SerializedName("held_usd")
    val heldUsd: String = "0.000000"
)

// Legacy alias for backward compatibility
typealias DashboardData = UsageResponse
