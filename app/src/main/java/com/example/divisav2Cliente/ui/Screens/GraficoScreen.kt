
package com.example.divisav2Cliente.ui.Screens
import android.view.ViewGroup
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavController
import com.example.divisav2Cliente.Data.Modelo.Moneda
import com.example.divisav2Cliente.ui.viewmodel.ExchangeViewModel
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.utils.ColorTemplate
import android.graphics.Color
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import java.text.SimpleDateFormat
import java.util.Locale

import java.util.Date

@Composable
fun GraficoScreen(viewModel: ExchangeViewModel, navController: NavController) {
    val exchangeRates by viewModel.exchangeRates.collectAsState()
    var selectedMoneda by remember { mutableStateOf<Moneda?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Encabezado con tipo de cambio
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Text(
                text = "1 ${exchangeRates.firstOrNull()?.currencyCode ?: "XXX"} = MXN ${exchangeRates.firstOrNull()?.exchangeRate ?: "N/A"}",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                color = MaterialTheme.colorScheme.onBackground,
                style = MaterialTheme.typography.headlineSmall,
                textAlign = TextAlign.Center
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // boton para volver
        Button(
            onClick = { navController.popBackStack() },
            modifier = Modifier.align(Alignment.Start)
        ) {
            Text("Volver")
        }

        Spacer(modifier = Modifier.height(20.dp))

        // la grafica a mostrar
        if (exchangeRates.isNotEmpty()) {
            LineChartView(exchangeRates) { moneda ->
                selectedMoneda = moneda
            }
        } else {
            Text(
                text = "No hay datos disponibles",
                modifier = Modifier.padding(16.dp),
                style = MaterialTheme.typography.bodyLarge
            )
        }

        // Mostrar datos en un TextField si hay un punto seleccionado
        selectedMoneda?.let { moneda ->
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = "Detalles del punto seleccionado:",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground
            )
            androidx.compose.material3.OutlinedTextField(
                value = """
                    Código: ${moneda.currencyCode}
                    Tasa: ${moneda.exchangeRate}
                    Base: ${moneda.baseCurrency}
                    Fecha: ${moneda.syncDate}
                """.trimIndent(),
                onValueChange = {},
                modifier = Modifier.fillMaxWidth(),
                readOnly = true
            )
        }
    }
}

@Composable
fun LineChartView(
    exchangeRates: List<Moneda>,
    onPointSelected: (Moneda) -> Unit
) {
    AndroidView(
        factory = { context ->
            LineChart(context).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )

                description.isEnabled = false
                setTouchEnabled(true)
                setPinchZoom(true)
                setDrawGridBackground(false)

                // Configurar eje X para mostrar fechas legibles
                xAxis.apply {
                    position = XAxis.XAxisPosition.BOTTOM
                    setDrawGridLines(false)
                    granularity = 1f // Evita valores intermedios incorrectos
                    textColor = Color.BLACK
                    labelRotationAngle = -45f // Rotar etiquetas para mejor visibilidad
                }

                axisLeft.apply {
                    setDrawGridLines(true)
                    textColor = Color.BLACK
                }
                axisRight.isEnabled = false

                // Listener para seleccionar un punto en la gráfica
                setOnChartValueSelectedListener(object : OnChartValueSelectedListener {
                    override fun onValueSelected(e: Entry?, h: Highlight?) {
                        e?.let { entry ->
                            val index = entry.x.toInt()
                            if (index in exchangeRates.indices) {
                                onPointSelected(exchangeRates[index])
                            }
                        }
                    }

                    override fun onNothingSelected() {}
                })
            }
        },
        update = { chart ->
            val entries = exchangeRates.mapIndexed { index, moneda ->
                Entry(index.toFloat(), moneda.exchangeRate.toFloat()) // Usar índice como posición en X
            }

            val syncDates = exchangeRates.map { it.syncDate } // Extraer fechas legibles
            chart.xAxis.valueFormatter = getDateFormatter(syncDates) // Asignar formateador de fechas

            val dataSet = LineDataSet(entries, "Tasa de Cambio").apply {
                setColors(ColorTemplate.JOYFUL_COLORS.toList())
                valueTextColor = Color.BLACK
                lineWidth = 2f
                circleRadius = 4f
                setDrawCircleHole(false)
                setDrawValues(true)
            }

            chart.data = LineData(dataSet)
            chart.invalidate() // Refrescar la gráfica
        },
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp)
    )
}


fun getDateFormatter(syncDates: List<String>): ValueFormatter {
    return object : ValueFormatter() {
        override fun getFormattedValue(value: Float): String {
            val index = value.toInt()
            return syncDates.getOrNull(index) ?: "-" // Evita errores por valores fuera de rango
        }
    }
}


