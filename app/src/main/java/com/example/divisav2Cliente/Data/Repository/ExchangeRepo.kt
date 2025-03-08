package com.example.divisav2Cliente.Data.Repository

import android.database.Cursor
import com.example.divisav2Cliente.Data.Dao.ExchangeDAO
import com.example.divisav2Cliente.Data.Entities.MonedaEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class ExchangeRepository @Inject constructor(
    private val exchangeDAO: ExchangeDAO
) {
    suspend fun getAll(): List<MonedaEntity> {
        return exchangeDAO.getAllMonedas()
    }

    fun getAllFlow(): Flow<List<MonedaEntity>> = exchangeDAO.getAllFlow()

    suspend fun insertAll(monedas: List<MonedaEntity>) {
        exchangeDAO.insertInfo(monedas)
    }

    suspend fun deleteAll() {
        exchangeDAO.deleteAllRates()
    }


    fun getExchangeRatesCursor(): Cursor {
        return exchangeDAO.getAllExchangeRatesCursor()
    }

    fun getExchangeRateByIdCursor(id: Long): Cursor {
        return exchangeDAO.getExchangeRateByIdCursor(id)
    }
}
