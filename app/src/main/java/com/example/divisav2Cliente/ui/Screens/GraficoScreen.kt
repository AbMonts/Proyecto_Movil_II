package com.example.divisav2Cliente.ui.Screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.Role.Companion.Button
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.divisav2Cliente.Data.Modelo.Moneda
import com.example.divisav2Cliente.ui.viewmodel.ExchangeViewModel

@Composable
fun GraficoScreen(viewModel: ExchangeViewModel, moneda: String, navController: NavController) {
    val exchangeRates by viewModel.exchangeRates.collectAsState()

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Button(
            onClick = { navController.popBackStack() },
            modifier = Modifier.padding(16.dp)
        ) {
            Text("Volver")
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (exchangeRates.isNotEmpty()) {
            LineChartCompose(exchangeRates)
        } else {
            Text("No hay datos disponibles", modifier = Modifier.padding(16.dp))
        }
    }
}

@Composable
fun LineChartCompose(exchangeRates: List<Moneda>) {
    val padding = 40f  // Espacio extra para los ejes
    val chartWidth = 300f
    val chartHeight = 200f

    // Normalización de los datos para que se ajusten al tamaño del gráfico
    val maxX = exchangeRates.size.toFloat() - 1
    val maxY = exchangeRates.maxOfOrNull { it.exchangeRate }?.toFloat() ?: 1f
    val minY = exchangeRates.minOfOrNull { it.exchangeRate }?.toFloat() ?: 0f

    val chartData = exchangeRates.mapIndexed { index, moneda ->
        val normalizedX = (index / maxX) * (chartWidth - padding)
        val normalizedY = ((moneda.exchangeRate.toFloat() - minY) / (maxY - minY)) * (chartHeight - padding)
        normalizedX to chartHeight - normalizedY  // Invertimos Y para que crezca hacia arriba
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp)
            .padding(16.dp)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            // Dibujar ejes
            drawLine(
                color = Color.Gray,
                start = Offset(padding, 0f),
                end = Offset(padding, chartHeight),
                strokeWidth = 2f
            )
            drawLine(
                color = Color.Gray,
                start = Offset(padding, chartHeight),
                end = Offset(chartWidth, chartHeight),
                strokeWidth = 2f
            )

            // Dibujar líneas de la gráfica
            val path = Path().apply {
                moveTo(padding + chartData.first().first, chartData.first().second)
                for (point in chartData.drop(1)) {
                    lineTo(padding + point.first, point.second)
                }
            }

            drawPath(
                path = path,
                color = Color.Blue,
                style = Stroke(width = 4f)
            )

            // Dibujar puntos en la gráfica
            chartData.forEach { point ->
                drawCircle(
                    color = Color.Red,
                    radius = 6f,
                    center = Offset(padding + point.first, point.second)
                )
            }
        }
    }
}

