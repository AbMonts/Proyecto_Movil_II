package com.example.divisav2.ViewModel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
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
import kotlinx.coroutines.launch
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
        observeDatabaseChanges() //pa que vea que tiene en la bd
        scheduleHourlySync() //la sincronizacion de cada hora
        checkWorkStatus()//para ver el estado del worker
    }


    //funciones pa registrar la hora de cada accion
    private fun actualizarFecha() { // Para la base de datos
        val fechaActual = System.currentTimeMillis() // Obtener el tiempo en milisegundos
        _ultimaInsercion.value = fechaActual
        Log.d("DB_Fecha", "Última actualización: ${java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date(fechaActual))}")
    }

    private fun actualizarConsultaApi() { // Para cuando se sincroniza con la API
        val fechaActual = System.currentTimeMillis()
        _ultimaConsultaApi.value = fechaActual
        Log.d("API_Consulta", "Ultima consulta a la API: ${java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date(fechaActual))}")
    }

    private fun observeDatabaseChanges() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.getAllFlow().collect { monedas ->
                _monedas.value = monedas
                actualizarFecha() // ultima fecha que guarda en bd
                logDatabase(monedas) // Mostrar en Logcat
            }
        }
    }



    fun insertAllExchanges(monedas: List<MonedaEntity>) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.insertAll(monedas)
        }
    }

//obtiene todas las monedas a partir del repositorio
    fun getAllExchanges() {
        viewModelScope.launch(Dispatchers.IO) {
            val monedasActualizadas = repository.getAll()
            _monedas.value = monedasActualizadas

            logDatabase(monedasActualizadas)
        }
    }

//funcion para el worker que se ejecuta cada hora y llama a la api con el worker
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
                .build()

            workManager.enqueue(workRequest)

            actualizarConsultaApi()
            // se  actualiza la ui
            viewModelScope.launch {
                kotlinx.coroutines.delay(3000)
                getAllExchanges()
            }
    }

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

    fun clearDatabase() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteAll()
            _monedas.value = emptyList()
            Log.d("DB_Clear", "Base de datos eliminada correctamente.")
        }
    }

    fun checkWorkStatus() {
        viewModelScope.launch(Dispatchers.IO) {
            val workInfos = workManager.getWorkInfosByTag("SyncExchangeWorker").get()

            workInfos.forEach { workInfo ->
                Log.d("WorkManagerStatus", "Work ID: ${workInfo.id}, Estado: ${workInfo.state}")
            }
        }
    }

}