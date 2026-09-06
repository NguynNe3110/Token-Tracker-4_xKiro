package com.uzuu.adminitratortoken.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.uzuu.adminitratortoken.data.local.entity.UsageRecordEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UsageRecordDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUsageRecord(record: UsageRecordEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(records: List<UsageRecordEntity>)

    @Query("SELECT * FROM usage_records WHERE accountId = :accountId ORDER BY timestamp DESC")
    fun getUsageByAccount(accountId: Long): Flow<List<UsageRecordEntity>>

    @Query("SELECT * FROM usage_records WHERE timestamp BETWEEN :startTime AND :endTime ORDER BY timestamp ASC")
    fun getUsageByDateRange(startTime: Long, endTime: Long): Flow<List<UsageRecordEntity>>

    @Query("SELECT * FROM usage_records WHERE accountId = :accountId AND timestamp BETWEEN :startTime AND :endTime ORDER BY timestamp ASC")
    fun getUsageByAccountAndDateRange(accountId: Long, startTime: Long, endTime: Long): Flow<List<UsageRecordEntity>>

    @Query("SELECT SUM(totalTokens) FROM usage_records WHERE timestamp BETWEEN :startTime AND :endTime")
    fun getTotalTokensInRange(startTime: Long, endTime: Long): Flow<Int?>

    @Query("DELETE FROM usage_records WHERE accountId = :accountId")
    suspend fun deleteByAccountId(accountId: Long)
}
