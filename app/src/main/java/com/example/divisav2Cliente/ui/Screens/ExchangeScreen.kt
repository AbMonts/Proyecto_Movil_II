package com.example.divisav2Cliente.ui.Screens

import android.app.DatePickerDialog
import android.app.TimePickerDialog
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

    val dateFormatter = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())

    var fechaInicioMillis by remember { mutableStateOf(System.currentTimeMillis()) }
    var fechaFinMillis by remember { mutableStateOf(System.currentTimeMillis()) }

    fun showDateTimePicker(context: Context, fechaActual: Long, onDateTimeSelected: (Long) -> Unit) {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = fechaActual

        // Primero, seleccionamos la fecha
        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                calendar.set(year, month, dayOfMonth)

                // Luego, mostramos el selector de hora
                TimePickerDialog(
                    context,
                    { _, hour, minute ->
                        calendar.set(Calendar.HOUR_OF_DAY, hour)
                        calendar.set(Calendar.MINUTE, minute)
                        calendar.set(Calendar.SECOND, 0)
                        calendar.set(Calendar.MILLISECOND, 0)

                        // Enviar el timestamp actualizado
                        onDateTimeSelected(calendar.timeInMillis)
                    },
                    calendar.get(Calendar.HOUR_OF_DAY),
                    calendar.get(Calendar.MINUTE),
                    true
                ).show()
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }


    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Consulta de Tasas de Cambio >:)",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.primary
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.medium,
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("Selecciona la Moneda", style = MaterialTheme.typography.bodyMedium)

                Box {
                    Button(
                        onClick = { expanded = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
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
                    // ----------------------------- Seleccion de fecha y hora ---------------------------------------------------
                Text("Desde la Fecha y hora de:", style = MaterialTheme.typography.bodyMedium)
                ElevatedButton(
                    onClick = { showDateTimePicker(context, fechaInicioMillis) { fechaInicioMillis = it } },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(dateFormatter.format(fechaInicioMillis))
                }

                Text("Hasta la fecha y hora de:", style = MaterialTheme.typography.bodyMedium)
                ElevatedButton(
                    onClick = { showDateTimePicker(context, fechaFinMillis) { fechaFinMillis = it } },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(dateFormatter.format(fechaFinMillis))
                }

            }
        }
            // --------------------------------- Boton para buscar y mostrar la grafica -------------------------------------------
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            ElevatedButton(
                onClick = {
                    viewModel.consultarExchangeRatesFiltrados(moneda, fechaInicioMillis, fechaFinMillis)
                    navController.navigate("chartScreen")
                },
                modifier = Modifier.weight(1f)
            ) {
                Text("Buscar")
            }

            // ---------------------------- Pa cargar todos los datos  ------------------------------
            Spacer(modifier = Modifier.width(8.dp))
            ElevatedButton(
                onClick = { viewModel.loadExchangeRates() },
                modifier = Modifier.weight(1f)
            ) {
                Text("Actualizar")
            }
        }

        // --------------------Lista de tasas de cambio -------------------------------------------------------

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val sortedExchangeRates = exchangeRates.sortedByDescending { it.syncDate } // Ordena por fecha descendente
            items(sortedExchangeRates) { moneda ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium,
                    elevation = CardDefaults.cardElevation(4.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Código: ${moneda.currencyCode}", style = MaterialTheme.typography.bodyLarge)
                        Text("Tasa: ${moneda.exchangeRate}", style = MaterialTheme.typography.bodyMedium)
                        Text("Base: ${moneda.baseCurrency}", style = MaterialTheme.typography.bodyMedium)
                        Text("Fecha: ${moneda.syncDate}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.secondary)
                    }
                }
            }
        }
    }
}
