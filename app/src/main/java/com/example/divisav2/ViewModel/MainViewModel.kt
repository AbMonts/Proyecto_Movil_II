package com.example.divisav2.ViewModel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.divisav2.Data.Entities.MonedaEntity
import com.example.divisav2.Data.Repository.ExchangeRepository
import com.example.divisav2.Workers.SyncExchangeWorker
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltViewModel
class MainViewModel@Inject constructor(
    private val repository: ExchangeRepository,

    private val workManager: WorkManager
) : ViewModel() {
    private val _monedas = MutableStateFlow<List<MonedaEntity>>(emptyList())
    val monedas: StateFlow<List<MonedaEntity>> get() = _monedas

    private val _ultimaInsercion = MutableStateFlow(0L) //para la base de datos
    val ultimaInsercion: StateFlow<Long> get() = _ultimaInsercion

    private val _ultimaConsultaApi = MutableStateFlow(0L) //para cuando se sincronice a la api
    val ultimaConsultaApi: StateFlow<Long> get() = _ultimaConsultaApi

    init {
        scheduleHourlySync() //la sincronizacion de cada hora
        checkWorkStatus()//para ver el estado del worker
    }

    //obtiene todas las monedas a partir del repositorio
    fun getAllExchanges() {
        viewModelScope.launch {
            repository.getAllFlow().collectLatest { monedasActualizadas ->
                _monedas.value = monedasActualizadas
            }
        }
    }


//funcion para el worker que se ejecuta cada hora y llama a la api con el worker SyncExchangeWorker.kt
    fun scheduleHourlySync() {
        val workRequest = PeriodicWorkRequestBuilder<SyncExchangeWorker>(
            1, TimeUnit.HOURS // Se ejec cada 1 hora
        )
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED) // Solo si hay internet
                    .build()
            )
            .build()

        workManager.enqueueUniquePeriodicWork(
            "HourlySyncWorker",
            ExistingPeriodicWorkPolicy.KEEP, // Evita duplicados
            workRequest
        )
        Log.d("WorkManager", "Sincronizacion programada cada hora")

        actualizarConsultaApi()
    }

//funcion para el worker que se ejecuta manualmente
    fun syncNow() {
        val workRequest = OneTimeWorkRequestBuilder<SyncExchangeWorker>()
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            )
            .addTag("ManualSync")
            .build()

        workManager.enqueueUniqueWork(
            "SyncNowWorker",
            ExistingWorkPolicy.KEEP, // Evita ejecuciones simultáneas
            workRequest
        )


        actualizarConsultaApi()

        workManager.getWorkInfoByIdLiveData(workRequest.id).observeForever { workInfo ->
            if (workInfo != null && workInfo.state.isFinished) {
                Log.d("SyncNow", "Sincronización manual terminada")
                viewModelScope.launch {
                    getAllExchanges() // Evita crear multiples llamadas a `collect`
                }
            }
        }
    }


    private fun actualizarConsultaApi() { // Para cuando se sincroniza con la API
        val fechaActual = System.currentTimeMillis()
        _ultimaConsultaApi.value = fechaActual
        Log.d("API_Consulta", "Ultima consulta a la API: ${java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date(fechaActual))}")
    }


    fun clearDatabase() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteAll()
            withContext(Dispatchers.Main) {
                _monedas.value = emptyList()
            }
            Log.d("DB_Clear", "Base de datos eliminada correctamente.")
        }
    }



    //para imprimir lo que se obtuvo de la bd
    private fun logDatabase(monedas: List<MonedaEntity>) {
        if (monedas.isEmpty()) {
            Log.d("DB_Info", "No hay datos en la base de datos :0 ")
        } else {
            monedas.forEach { moneda ->
                Log.d(
                    "DB_Datos", "Código: ${moneda.currencyCode}, " +
                            "Tasa: ${moneda.exchangeRate}, " +
                            "Base: ${moneda.baseCurrency}, " +
                            "Fecha: ${moneda.syncDate}"
                )
            }
        }
    }

    //solo prueba dell worker
    fun checkWorkStatus() {
        viewModelScope.launch(Dispatchers.IO) {
            val workInfos = workManager.getWorkInfosByTag("SyncExchangeWorker").get()

            workInfos.forEach { workInfo ->
                Log.d("WorkManagerStatus", "Work ID: ${workInfo.id}, Estado: ${workInfo.state}")
            }
        }
    }

}