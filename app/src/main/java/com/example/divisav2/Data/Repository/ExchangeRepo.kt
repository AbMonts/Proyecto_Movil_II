package com.example.divisav2.Data.Repository

import com.example.divisav2.APIService.ExchangeAPI
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

    suspend fun syncData() {
        try {
            val apiResponse = ExchangeAPI.service.getExchangeRates()

            val baseCurrency = apiResponse.baseCode
            val timestamp = System.currentTimeMillis()
            val syncDate = java.text.SimpleDateFormat("yyyy-MM-dd").format(java.util.Date())


            val rates = apiResponse.conversionRates.map { (currency, rate) ->
                MonedaEntity(
                    currencyCode = currency,
                    exchangeRate = rate,
                    baseCurrency = baseCurrency,
                    timestamp = timestamp,
                    syncDate = syncDate
                )
            }


            deleteAll()
            insertAll(rates)

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
