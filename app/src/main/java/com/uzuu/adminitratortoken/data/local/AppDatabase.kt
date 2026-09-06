package com.uzuu.adminitratortoken.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.uzuu.adminitratortoken.data.local.dao.AccountDao
import com.uzuu.adminitratortoken.data.local.dao.UsageRecordDao
import com.uzuu.adminitratortoken.data.local.entity.AccountEntity
import com.uzuu.adminitratortoken.data.local.entity.UsageRecordEntity

@Database(
    entities = [AccountEntity::class, UsageRecordEntity::class],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun accountDao(): AccountDao
    abstract fun usageRecordDao(): UsageRecordDao
}
