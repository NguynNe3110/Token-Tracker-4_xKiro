package com.uzuu.adminitratortoken.di

import android.content.Context
import androidx.room.Room
import com.uzuu.adminitratortoken.data.local.AppDatabase
import com.uzuu.adminitratortoken.data.local.dao.AccountDao
import com.uzuu.adminitratortoken.data.local.dao.UsageRecordDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "token_tracker_db"
        ).fallbackToDestructiveMigration(true).build()
    }

    @Provides
    @Singleton
    fun provideAccountDao(database: AppDatabase): AccountDao {
        return database.accountDao()
    }

    @Provides
    @Singleton
    fun provideUsageRecordDao(database: AppDatabase): UsageRecordDao {
        return database.usageRecordDao()
    }
}
