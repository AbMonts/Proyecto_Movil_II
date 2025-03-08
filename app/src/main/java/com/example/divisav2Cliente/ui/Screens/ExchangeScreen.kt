package com.example.divisav2Cliente.ui.Screens

import android.app.DatePickerDialog
import android.content.Context
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavController
import com.example.divisav2Cliente.Data.Modelo.Moneda
import com.example.divisav2Cliente.ui.viewmodel.ExchangeViewModel


@Composable
fun ExchangeScreen(viewModel: ExchangeViewModel, navController: NavController) {
    val exchangeRates by viewModel.exchangeRates.collectAsState()
    val context = LocalContext.current

    var moneda by remember { mutableStateOf("MXN") }
    var expanded by remember { mutableStateOf(false) }
    val monedasDisponibles = listOf("MXN", "USD", "EUR", "JPY", "GBP")

    var fechaMillis by remember { mutableStateOf(System.currentTimeMillis()) }
    var mostrarGrafica by remember { mutableStateOf(false) } // Estado para mostrar/ocultar gráfica

    val dateFormatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    fun showDatePicker(context: Context, onDateSelected: (Long) -> Unit) {
        val calendar = Calendar.getInstance()
        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                calendar.set(year, month, dayOfMonth)
                onDateSelected(calendar.timeInMillis)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(text = "Selecciona la Moneda:")

        Box {
            Button(onClick = { expanded = true }, modifier = Modifier.fillMaxWidth()) {
                Text(moneda)
            }

            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                monedasDisponibles.forEach { item ->
                    DropdownMenuItem(
                        text = { Text(item) },
                        onClick = {
                            moneda = item
                            expanded = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = { showDatePicker(context) { fechaMillis = it } },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Fecha: ${dateFormatter.format(fechaMillis)}")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
//                viewModel.consultarExchangeRatesFiltrados(moneda, fechaMillis)
//                mostrarGrafica = true // Mostrar la gráfica cuando se buscan datos
                viewModel.consultarExchangeRatesFiltrados(moneda, fechaMillis)
                navController.navigate("chartScreen/$moneda")
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Buscar")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { mostrarGrafica = false }, // Ocultar la gráfica
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Ocultar Gráfica")
        }

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn {
            items(exchangeRates) { moneda ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Código: ${moneda.currencyCode}")
                        Text("Tasa: ${moneda.exchangeRate}")
                        Text("Base: ${moneda.baseCurrency}")
                        Text("Fecha: ${moneda.syncDate}")
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (mostrarGrafica && exchangeRates.isNotEmpty()) {

        }
    }


}
@Composable
fun ChartScreen(viewModel: ExchangeViewModel, moneda: String, navController: NavController) {
    val exchangeRates by viewModel.exchangeRates.collectAsState()

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Button(
            onClick = { navController.popBackStack() },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Volver")
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (exchangeRates.isNotEmpty()) {
            LineChartCompose(exchangeRates)
        } else {
            Text("No hay datos disponibles para $moneda")
        }
    }
}


