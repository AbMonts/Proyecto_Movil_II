package com.example.divisav2.di

import com.example.divisav2.Data.Dao.ExchangeDAO
import com.example.divisav2.Data.Repository.ExchangeRepository
import com.example.divisav2.Workers.SyncExchangeWorker
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun provideExchangeRepository(exchangeDAO: ExchangeDAO): ExchangeRepository {
        return ExchangeRepository(exchangeDAO)
    }


}