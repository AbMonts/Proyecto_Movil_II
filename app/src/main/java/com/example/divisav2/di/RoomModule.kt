package com.example.divisav2.di

import android.content.Context
import androidx.room.Room
import androidx.work.WorkManager
import com.example.divisav2.Data.Dao.ExchangeDAO
import com.example.divisav2.Data.DataBase.ExchangeDB
import com.example.divisav2.Data.Repository.ExchangeRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RoomModule {

    private const val DATABASE_NAME = "exchange_database"
    @Singleton
    @Provides
    fun provideRoom(@ApplicationContext context: Context) =
        Room.databaseBuilder(context, ExchangeDB::class.java,
            DATABASE_NAME).build()

    @Singleton
    @Provides
    fun provideExchangeDAO(db: ExchangeDB) = db.exchangeDAO()


    @Provides
    @Singleton
    fun provideExchangeRepository(exchangeDAO: ExchangeDAO): ExchangeRepository {
        return ExchangeRepository(exchangeDAO)
    }

    @Provides
    @Singleton
    fun provideWorkManager(@ApplicationContext context: Context): WorkManager {
        return WorkManager.getInstance(context)
    }


}