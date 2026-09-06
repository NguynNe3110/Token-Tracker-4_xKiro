package com.uzuu.adminitratortoken.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "usage_records",
    foreignKeys = [
        ForeignKey(
            entity = AccountEntity::class,
            parentColumns = ["id"],
            childColumns = ["accountId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["accountId"]), Index(value = ["timestamp"])]
)
data class UsageRecordEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val accountId: Long,
    val promptTokens: Int,
    val completionTokens: Int,
    val totalTokens: Int,
    val limitPerDay: Long = 0L,
    val model: String = "",
    val timestamp: Long = System.currentTimeMillis()
)
