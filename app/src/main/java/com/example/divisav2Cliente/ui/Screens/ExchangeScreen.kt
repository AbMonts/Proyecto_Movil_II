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
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {

        Text(
            text = "Consulta de Tasas de Cambio >:)",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.primary
        )

        //la selec de la moneda y fecha
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

                ElevatedButton(
                    onClick = { showDatePicker(context) { fechaMillis = it } },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Fecha: ${dateFormatter.format(fechaMillis)}")
                }
            }
        }

        // los botones, buscar, actualizar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            ElevatedButton(
                onClick = {
                    viewModel.consultarExchangeRatesFiltrados(moneda, fechaMillis)
                    navController.navigate("chartScreen")
                },
                modifier = Modifier.weight(1f)
            ) {
                Text("Buscar")
            }
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
            items(exchangeRates) { moneda ->
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
