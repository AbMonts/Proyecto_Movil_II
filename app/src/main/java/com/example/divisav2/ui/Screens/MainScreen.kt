package com.example.divisav2.ui.Screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.divisav2.Data.Entities.MonedaEntity
import com.example.divisav2.ViewModel.MainViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainScreen {

}
@Composable
fun MainScreen(viewModel: MainViewModel) {
    val monedasState = viewModel.monedas.collectAsState()
    val ultimaInsercionState = viewModel.ultimaInsercion.collectAsState()
    val ultimaConsultaApiState = viewModel.ultimaConsultaApi.collectAsState()

    Scaffold(
        topBar = {
            Text("Divisas")
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Botones de acciones
            Button(
                onClick = { viewModel.getAllExchanges() },
                modifier = Modifier.padding(16.dp)
            ) {
                Text("Cargar Monedas")
            }
            Button(
                onClick = { viewModel.syncNow() },
                modifier = Modifier.padding(16.dp)
            ) {
                Text("Sincronizar Ahora")
            }
            Button(
                onClick = { viewModel.clearDatabase() },
                modifier = Modifier.padding(16.dp)
            ) {
                Text("Eliminar Datos")
            }

            // fechas
            Text(
                text = "Ultima insercion en BD: ${formatTimestamp(ultimaInsercionState.value)}",
                modifier = Modifier.padding(16.dp)
            )
            Text(
                text = "Ultima consulta a la API: ${formatTimestamp(ultimaConsultaApiState.value)}",
                modifier = Modifier.padding(16.dp)
            )

            // Lista de monedas
            LazyColumn(modifier = Modifier.padding(16.dp)) {
                items(monedasState.value) { moneda ->
                    MonedaItem(moneda)
                }
            }
        }
    }
}


@Composable
fun MonedaItem(moneda: MonedaEntity) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = "Código: ${moneda.currencyCode}", fontWeight = FontWeight.Bold)
            Text(text = "Tasa: ${moneda.exchangeRate}")
            Text(text = "Base: ${moneda.baseCurrency}")
            Text(text = "Fecha: ${moneda.syncDate}")
        }
    }
}

fun formatTimestamp(timestamp: Long): String {
    return if (timestamp > 0) {
        SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date(timestamp))
    } else {
        "Hay... no sé."
    }
}
