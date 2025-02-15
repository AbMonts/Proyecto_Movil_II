package com.example.divisav2

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.lifecycleScope
import com.example.divisav2.APIService.ExchangeAPI
import com.example.divisav2.Application.RoomApp
import com.example.divisav2.Data.Entities.MonedaEntity
import com.example.divisav2.Data.Modelo.Moneda
import com.example.divisav2.Data.Repository.ExchangeRepository
import com.example.divisav2.ViewModel.MainViewModel
import com.example.divisav2.ViewModel.MainViewModelFactory
import com.example.divisav2.ui.Screens.MainScreen
import com.example.divisav2.ui.theme.DivisaV2Theme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels {
        MainViewModelFactory(RoomApp.database.exchangeDAO())
    }

    override fun onCreate(savedInstanceState: Bundle?) {


        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            DivisaV2Theme {
                MainScreen(viewModel)
            }
        }
         //fetchExchangeRates()//desde la api, obtiene datis
        fetchSavedData() //desde la base de datos localll con room
        }


    private fun fetchExchangeRates() {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                // Realiza la llamada a la API
                val response = ExchangeAPI.service.getExchangeRates()

                // Convierte el timestamp en formato legible
                val syncDate = formatUnixTimestamp(response.timestamp)

                // Mapea la respuesta a una lista de monedaEntity
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

                // Inserta las monedas en la base de datos
                viewModel.insertAllExchanges(monedas)
                Log.d("DB_SAVE", "Datos guardados correctamente en la base de datos")

            } catch (e: Exception) {
                Log.e("API_ERROR", "Error al obtener tasas de cambio: ${e.message}")
            }
        }
    }

    private fun fetchSavedData() {
        lifecycleScope.launch {
            try {
                viewModel.getAllExchanges()

                viewModel.monedas.collectLatest { monedas -> // da solo el último valor, con solo collect seria todos :0
                    if (monedas.isEmpty()) {
                        Log.d("DB_Info", "No hay datos en la base de datos.")
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
                Log.d("DB_FETCH", "----------- Datos obtenidos correctamente de la base de datos :)")
            } catch (e: Exception) {
                Log.e("DB_ERROR", "Error al obtener los datos de la base de datos: ${e.message}")
            }
        }
    }


    private fun formatUnixTimestamp(timestamp: Long): String {
        val date = java.util.Date(timestamp * 1000)
        val format = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
        return format.format(date)
    }
}

