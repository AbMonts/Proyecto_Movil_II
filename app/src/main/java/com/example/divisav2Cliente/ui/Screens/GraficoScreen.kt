package com.example.divisav2Cliente.ui.Screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.Role.Companion.Button
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.divisav2Cliente.Data.Modelo.Moneda
import com.example.divisav2Cliente.ui.viewmodel.ExchangeViewModel


@Composable
fun GraficoScreen(viewModel: ExchangeViewModel, navController: NavController) {
    val exchangeRates by viewModel.exchangeRates.collectAsState()

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(20.dp)) // Espacio antes del botón

        Button(
            onClick = { navController.popBackStack() },
            modifier = Modifier
                .align(Alignment.Start)
                .padding(start = 30.dp, bottom = 10.dp)
        ) {
            Text("Volver")
        }

        Spacer(modifier = Modifier.height(20.dp))

        if (exchangeRates.isNotEmpty()) {
            LineChartCompose(exchangeRates)
        } else {
            Text("No hay datos disponibles", modifier = Modifier.padding(16.dp))
        }
    }
}
@Composable
fun LineChartCompose(exchangeRates: List<Moneda>) {
    val paddingX = 120f
    val paddingY = 80f
    val chartWidth = 1000f
    val chartHeight = 600f

    val maxX = (exchangeRates.size - 1).coerceAtLeast(1).toFloat()
    val maxY = exchangeRates.maxOfOrNull { it.exchangeRate }?.toFloat() ?: 1f
    val minY = exchangeRates.minOfOrNull { it.exchangeRate }?.toFloat() ?: 0f

    val chartData = exchangeRates.mapIndexed { index, moneda ->
        val normalizedX = (index.toFloat() / maxX) * (chartWidth - paddingX)
        val normalizedY = ((moneda.exchangeRate.toFloat() - minY) / (maxY - minY)) * (chartHeight - paddingY)
        normalizedX to chartHeight - normalizedY
    }

    var selectedPoint by remember { mutableStateOf<Int?>(null) }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(600.dp)
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTapGestures(
                        onPress = { offset ->
                            val tappedIndex = chartData.indexOfFirst { (x, y) ->
                                val dx = (paddingX + x) - offset.x
                                val dy = y - offset.y
                                dx * dx + dy * dy <= 400
                            }
                            selectedPoint = if (tappedIndex != -1) tappedIndex else null
                        }
                    )
                }
            ) {
                // Dibujar ejes
                drawLine(
                    color = Color.Gray,
                    start = Offset(paddingX, paddingY),
                    end = Offset(paddingX, chartHeight),
                    strokeWidth = 4f
                )
                drawLine(
                    color = Color.Gray,
                    start = Offset(paddingX, chartHeight),
                    end = Offset(chartWidth, chartHeight),
                    strokeWidth = 4f
                )

                // Etiquetas del eje Y
                val stepY = (maxY - minY) / 6
                for (i in 0..6) {
                    val value = minY + (stepY * i)
                    val yOffset = chartHeight - ((value - minY) / (maxY - minY)) * (chartHeight - paddingY)
                    drawContext.canvas.nativeCanvas.drawText(
                        "%.2f".format(value),
                        30f, yOffset,
                        android.graphics.Paint().apply {
                            textSize = 35f
                            color = android.graphics.Color.BLACK
                        }
                    )
                }

                // Dibujar líneas de la gráfica
                val path = Path().apply {
                    moveTo(paddingX + chartData.first().first, chartData.first().second)
                    for (point in chartData.drop(1)) {
                        lineTo(paddingX + point.first, point.second)
                    }
                }
                drawPath(path, color = Color.Blue, style = Stroke(width = 6f))

                // Dibujar puntos
                chartData.forEachIndexed { index, point ->
                    drawCircle(
                        color = if (selectedPoint == index) Color.Green else Color.Red,
                        radius = 10f,
                        center = Offset(paddingX + point.first, point.second)
                    )
                }

                // Etiqueta de la fecha en el eje X solo si un punto está seleccionado
                selectedPoint?.let { index ->
                    val moneda = exchangeRates[index]
                    val xOffset = paddingX + chartData[index].first
                    drawContext.canvas.nativeCanvas.drawText(
                        moneda.syncDate,
                        xOffset, chartHeight + 40f,
                        android.graphics.Paint().apply {
                            textSize = 35f
                            color = android.graphics.Color.BLACK
                            textAlign = android.graphics.Paint.Align.CENTER
                        }
                    )
                }
            }
        }

        // Información del punto seleccionado debajo de la gráfica
        selectedPoint?.let { index ->
            val moneda = exchangeRates[index]
            val labelText = "1 (base) = ${moneda.currencyCode}: ${moneda.exchangeRate}"

            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Moneda: ${moneda.currencyCode} | Tasa: ${moneda.exchangeRate} | Fecha: ${moneda.syncDate}",
                    color = Color.Black
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = labelText,
                    color = Color.Blue
                )
            }
        }
    }
}
