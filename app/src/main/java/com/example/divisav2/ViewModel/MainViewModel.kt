package com.example.divisav2.ViewModel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.divisav2.Data.Dao.ExchangeDAO
import com.example.divisav2.Data.Entities.MonedaEntity
import com.example.divisav2.Data.Repository.ExchangeRepository
import com.example.divisav2.Workers.SyncExchangeWorker
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel@Inject constructor(
    private val repository: ExchangeRepository,
    private val workManager: WorkManager
) : ViewModel() {
    private val _monedas = MutableStateFlow<List<MonedaEntity>>(emptyList())
    val monedas: StateFlow<List<MonedaEntity>> get() = _monedas

    private val _ultimaActualizacion = MutableStateFlow("")
    val ultimaActualizacion: StateFlow<String> get() = _ultimaActualizacion

    init {
        observeDatabaseChanges() // Observar cambios en la BD
    }

    private fun observeDatabaseChanges() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.getAllFlow().collect { monedas ->
                _monedas.value = monedas
                actualizarFecha() // Guardar la última fecha
                logDatabase(monedas) // Mostrar en Logcat
            }
        }
    }

    fun insertAllExchanges(monedas: List<MonedaEntity>) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.insertAll(monedas)
        }
    }


    fun getAllExchanges() {
        viewModelScope.launch(Dispatchers.IO) {
            val monedasActualizadas = repository.getAll()
            _monedas.value = monedasActualizadas
            actualizarFecha() // Guardar la última fecha
            logDatabase(monedasActualizadas) // Mostrar en Logcat
        }
    }

    fun syncNow() {
        val workRequest = OneTimeWorkRequestBuilder<SyncExchangeWorker>()
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED) // Solo se ejecuta si hay internet
                    .build()
            )
            .build()

        workManager.enqueue(workRequest)

        // Esperar un poco y actualizar la UI después de la sincronización
        viewModelScope.launch {
            kotlinx.coroutines.delay(3000) // Esperar un poco para que se complete el Worker
            getAllExchanges() // Cargar los datos actualizados en la UI
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

    private fun actualizarFecha() {
        val fechaActual = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date())
        _ultimaActualizacion.value = fechaActual
        Log.d("DB_Fecha", "Última actualización: $fechaActual")
    }
}