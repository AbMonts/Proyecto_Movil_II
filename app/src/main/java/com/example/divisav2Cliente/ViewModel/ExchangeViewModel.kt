package com.example.divisav2Cliente.ui.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.divisav2Cliente.Data.Modelo.Moneda
import getAllExchanges
import getFilteredExchanges
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

class ExchangeViewModel(application: Application) : AndroidViewModel(application) {

    private val _exchangeRates = MutableStateFlow<List<Moneda>>(emptyList())
    val exchangeRates: StateFlow<List<Moneda>> = _exchangeRates

    init {
        loadExchangeRates()
    }
    fun loadExchangeRates() {
        viewModelScope.launch {
            val exchangeList = getAllExchanges(getApplication<Application>().applicationContext)
            Log.d("ExchangeViewModel", "Carga automática: ${exchangeList.size} registros")
            _exchangeRates.emit(exchangeList)
        }
    }


    fun consultarExchangeRatesFiltrados(moneda: String, fecha: Long) {
        viewModelScope.launch {
            val fechaInicio = convertirFechaATimestampUTC(fecha, true)  // ← Convertimos a UTC
            val fechaFin = convertirFechaATimestampUTC(fecha, false)   // ← Convertimos a UTC

            // Convierte a formato legible para depurar
            val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            sdf.timeZone = TimeZone.getTimeZone("UTC")  // ← Asegura que sea UTC

            Log.d("ExchangeViewModel", "Consulta filtrada: moneda=$moneda")
            Log.d("ExchangeViewModel", "Fecha inicio: ${sdf.format(Date(fechaInicio))} ($fechaInicio)")
            Log.d("ExchangeViewModel", "Fecha fin: ${sdf.format(Date(fechaFin))} ($fechaFin)")

            val resultados = getFilteredExchanges(getApplication<Application>().applicationContext, moneda, fechaInicio, fechaFin)

            Log.d("ExchangeViewModel", "Consulta filtrada: ${resultados.size} registros")
            resultados.forEach { Log.d("ExchangeViewModel", "Registro obtenido: $it") }
            _exchangeRates.emit(resultados)
        }
    }


    /**
     * Convierte un timestamp a la zona horaria CST y ajusta la hora al inicio o fin del día.
     */
    fun convertirFechaATimestampUTC(fechaMillis: Long, esInicioDelDia: Boolean): Long {
        val calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"))  // ← CAMBIADO A UTC
        calendar.timeInMillis = fechaMillis

        if (esInicioDelDia) {
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)
        } else {
            calendar.set(Calendar.HOUR_OF_DAY, 23)
            calendar.set(Calendar.MINUTE, 59)
            calendar.set(Calendar.SECOND, 59)
            calendar.set(Calendar.MILLISECOND, 999)
        }

        return calendar.timeInMillis  // ← Esto asegurará que los timestamps sean correctos en UTC
    }
}
