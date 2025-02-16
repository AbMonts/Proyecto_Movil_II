package com.example.divisav2.Workers

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.example.divisav2.APIService.ExchangeAPI
import com.example.divisav2.Data.Entities.MonedaEntity
import com.example.divisav2.Data.Repository.ExchangeRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.runBlocking
import java.util.Date

@HiltWorker
class SyncExchangeWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParameters: WorkerParameters,
    private val repository: ExchangeRepository // Inyectar el repositorio
) : Worker(context, workerParameters) {

    override fun doWork(): Result {
        Log.d("SyncExchangeWorker", "Sincronizando tasas de cambio...")

        return try {
            syncExchangeRates()
            Result.success()
        } catch (e: Exception) {
            Log.e("SyncExchangeWorker", "Error sincronizando tasas de cambio", e)
            Result.retry() // Reintentar si falla
        }
    }

    private fun syncExchangeRates() {
        Log.d("SyncExchangeWorker", "Llamando a la API para actualizar tasas de cambio...")

        runBlocking { // Bloquear el Worker hasta que termine la ejecución
            try {
                val response = ExchangeAPI.service.getExchangeRates() // Llamada a la API
                // Definir la fecha de sincronización antes de usarla
                val syncDate = Date().toString()

                val monedas = response.conversionRates.map { (code, rate) ->
                    MonedaEntity(
                        currencyCode = code,
                        exchangeRate = rate,
                        baseCurrency = response.baseCode,
                        timestamp = response.timestamp,
                        syncDate = syncDate
                    )
                }
                // Imprimir en Logcat
                monedas.forEach { moneda ->
                    Log.d("EXCHANGE_DATA", "Código: ${moneda.currencyCode}, " +
                            "Tasa: ${moneda.exchangeRate}, " +
                            "Base: ${moneda.baseCurrency}, " +
                            "Fecha: ${moneda.syncDate}")
                }
                Log.d("SyncExchangeWorker", "Sincronización completada: ${monedas.size} monedas guardadas.")

                repository.insertAll(monedas) // Guardar en la BD
                Log.d("SyncExchangeWorker", "Sincronización completada: ${monedas.size} monedas guardadas.")
            } catch (e: Exception) {
                Log.e("SyncExchangeWorker", "Error obteniendo datos de la API", e)
                throw e
            }
        }
    }



}
