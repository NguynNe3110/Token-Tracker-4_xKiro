package com.uzuu.adminitratortoken.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "accounts")
data class AccountEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val providerType: String,
    val apiKeyAlias: String,
    val baseUrl: String,
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)
