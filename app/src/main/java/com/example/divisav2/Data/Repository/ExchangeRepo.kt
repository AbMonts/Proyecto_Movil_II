package com.example.divisav2.Data.Repository

import android.database.Cursor
import androidx.sqlite.db.SimpleSQLiteQuery
import androidx.sqlite.db.SupportSQLiteQuery
import com.example.divisav2.Data.Dao.ExchangeDAO
import com.example.divisav2.Data.Entities.MonedaEntity
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

    fun getFilteredExchangeRatesCursor(currencyCode: String, fechaInicio: Long, fechaFin: Long): Cursor {
        return exchangeDAO.getFilteredExchangeRatesCursor(currencyCode, fechaInicio, fechaFin)
    }


}
