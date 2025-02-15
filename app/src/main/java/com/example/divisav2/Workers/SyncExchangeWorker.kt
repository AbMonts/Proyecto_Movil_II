package com.example.divisav2.Workers

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.divisav2.APIService.ExchangeAPI
import com.example.divisav2.Application.RoomApp
import com.example.divisav2.Data.Entities.MonedaEntity
import com.example.divisav2.Data.Repository.ExchangeRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

class SyncExchangeWorker(ctx: Context, params: WorkerParameters) : CoroutineWorker(ctx, params) {

    private val repository: ExchangeRepository by lazy {
        val dao = RoomApp.database.exchangeDAO()
        ExchangeRepository(dao)
    }

    override suspend fun doWork(): Result {
        return try {
            withContext(Dispatchers.IO) {
                Log.d("SyncExchangeWorker", "Iniciando sincronización con la API...")

                val response = ExchangeAPI.service.getExchangeRates()

                val syncDate = formatUnixTimestamp(response.timestamp)

                val monedas = response.conversionRates.map { (code, rate) ->
                    MonedaEntity(
                        currencyCode = code,
                        exchangeRate = rate,
                        baseCurrency = response.baseCode,
                        timestamp = response.timestamp,
                        syncDate = syncDate
                    )
                }

                repository.deleteAll()  // Elimina registros antiguos
                repository.insertAll(monedas)  // Guarda los nuevos datos en Room

                Log.d("SyncExchangeWorker", "Sincronización exitosa. Datos guardados en la BD.")
                Result.success()
            }
        } catch (e: Exception) {
            Log.e("SyncExchangeWorker", "Error al sincronizar datos: ${e.message}")
            Result.retry()  // Reintentar en caso de error
        }
    }



    private fun formatUnixTimestamp(timestamp: Long): String {
        val date = Date(timestamp * 1000)
        val format = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return format.format(date)
    }
}
