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
    val paddingX = 120f  // Más espacio para etiquetas del eje Y
    val paddingY = 80f   // Más espacio superior
    val chartWidth = 800f  // Aumento del tamaño de la gráfica
    val chartHeight = 500f

    val maxX = (exchangeRates.size - 1).coerceAtLeast(1).toFloat()
    val maxY = exchangeRates.maxOfOrNull { it.exchangeRate }?.toFloat() ?: 1f
    val minY = exchangeRates.minOfOrNull { it.exchangeRate }?.toFloat() ?: 0f

    val chartData = exchangeRates.mapIndexed { index, moneda ->
        val normalizedX = (index / maxX) * (chartWidth - paddingX)
        val normalizedY = ((moneda.exchangeRate.toFloat() - minY) / (maxY - minY)) * (chartHeight - paddingY)
        normalizedX to chartHeight - normalizedY
    }

    var selectedPoint by remember { mutableStateOf<Int?>(null) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(550.dp)  // Ajustado para más espacio
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
                            dx * dx + dy * dy <= 400  // Aumentado el radio de detección táctil
                        }
                        if (tappedIndex != -1) {
                            selectedPoint = tappedIndex
                        }
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

            // Etiquetas del eje Y (tasas de cambio)
            val stepY = (maxY - minY) / 5
            for (i in 0..5) {
                val value = minY + (stepY * i)
                val yOffset = chartHeight - ((value - minY) / (maxY - minY)) * (chartHeight - paddingY)
                drawContext.canvas.nativeCanvas.drawText(
                    "%.2f".format(value),
                    30f, yOffset,
                    android.graphics.Paint().apply {
                        textSize = 40f  // Tamaño de fuente aumentado
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

            // Dibujar puntos y mostrar información cuando se selecciona uno
            chartData.forEachIndexed { index, point ->
                val moneda = exchangeRates[index]

                drawCircle(
                    color = if (selectedPoint == index) Color.Green else Color.Red,
                    radius = 10f,  // Puntos más grandes
                    center = Offset(paddingX + point.first, point.second)
                )

                if (selectedPoint == index) {
                    // Etiqueta con código y tasa de cambio (arriba de la gráfica)
                    val labelText = "1 (base) = ${moneda.currencyCode}: ${moneda.exchangeRate}"
                    drawContext.canvas.nativeCanvas.drawText(
                        labelText,
                        paddingX + point.first, paddingY - 40,
                        android.graphics.Paint().apply {
                            textSize = 45f  // Aumentado tamaño de la fuente
                            color = android.graphics.Color.BLACK
                        }
                    )

                    // Etiqueta de fecha (debajo de la gráfica)
                    val dateText = "Última fecha: ${moneda.syncDate}"
                    drawContext.canvas.nativeCanvas.drawText(
                        dateText,
                        paddingX + point.first, chartHeight + 50,
                        android.graphics.Paint().apply {
                            textSize = 35f  // Aumentado tamaño de la fuente
                            color = android.graphics.Color.DKGRAY
                        }
                    )
                }
            }
        }
    }
}

