package com.sentinel.di

import android.content.Context
import androidx.room.Room
import com.sentinel.core.crypto.KeyStoreManager
import com.sentinel.data.local.db.AppDatabase
import com.sentinel.data.local.db.dao.AppConfigDao
import com.sentinel.data.local.db.dao.TheftLogDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import net.sqlcipher.database.SupportFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        val passphrase = KeyStoreManager.getDbPassphrase()
        val factory = SupportFactory(passphrase)
        
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            AppDatabase.DATABASE_NAME
        ).openHelperFactory(factory)
            .build()
    }

    @Provides
    fun provideTheftLogDao(database: AppDatabase): TheftLogDao {
        return database.theftLogDao()
    }

    @Provides
    fun provideAppConfigDao(database: AppDatabase): AppConfigDao {
        return database.appConfigDao()
    }
}
