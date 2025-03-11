
package com.example.divisav2Cliente.ui.Screens
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.divisav2Cliente.Data.Modelo.Moneda
import com.example.divisav2Cliente.ui.viewmodel.ExchangeViewModel
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun GraficoScreen(viewModel: ExchangeViewModel, navController: NavController) {
    val exchangeRates by viewModel.exchangeRates.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // ------------encabezado con el tipo de cambio -----------------------
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

        // ------------- boton para volver a Exchangescreen.kt --------------------------------
        Button(
            onClick = { navController.popBackStack() },
            modifier = Modifier.align(Alignment.Start)
        ) {
            Text("Volver")
        }

        Spacer(modifier = Modifier.height(20.dp))

        // ------------ aca se mostrara la grafica con los datos filtrados
        if (exchangeRates.isNotEmpty()) {
            LineChartCompose(exchangeRates)
        } else {
            Text(
                text = "No hay datos disponibles",
                modifier = Modifier.padding(16.dp),
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}

//--------- ----------------- funci para construir la grafica (con datos filtrados)  ----------------
@Composable
fun LineChartCompose(exchangeRates: List<Moneda>) {
    val paddingX = 100f
    val paddingY = 80f
    val chartWidth = 1000f
    val chartHeight = 600f

    val maxX = (exchangeRates.size - 1).coerceAtLeast(1).toFloat()
    val maxY = (exchangeRates.maxOfOrNull { it.exchangeRate }?.toFloat() ?: 1f) * 1.1f
    val minY = 0f

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
            Canvas(
                modifier = Modifier
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
                // ---------------------------- fondo cuadriculado -----------------------------------
                val gridColor = Color.LightGray
                val gridSpacingX = (chartWidth - paddingX) / 5
                val gridSpacingY = (chartHeight - paddingY) / 6

                for (i in 0..5) {
                    val x = paddingX + i * gridSpacingX
                    drawLine(gridColor, Offset(x, paddingY), Offset(x, chartHeight), strokeWidth = 2f)
                }

                for (i in 0..6) {
                    val y = chartHeight - i * gridSpacingY
                    drawLine(gridColor, Offset(paddingX, y), Offset(chartWidth, y), strokeWidth = 2f)
                }

                // dibujar ejes
                drawLine(Color.Gray, Offset(paddingX, paddingY), Offset(paddingX, chartHeight), strokeWidth = 4f)
                drawLine(Color.Gray, Offset(paddingX, chartHeight), Offset(chartWidth, chartHeight), strokeWidth = 4f)

                // --------- Dibujar etiquetas en el eje y ---------------
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

                // -----------------La linea de la grafica -------------------------------------
                val path = Path().apply {
                    moveTo(paddingX + chartData.first().first, chartData.first().second)
                    for (point in chartData.drop(1)) {
                        lineTo(paddingX + point.first, point.second)
                    }
                }
                drawPath(path, color = Color.Blue, style = Stroke(width = 6f))

                //------------- los puntos de los datos filtrados ------------------------------
                chartData.forEachIndexed { index, point ->
                    val pointOffset = Offset(paddingX + point.first, point.second)

                    drawCircle(
                        color = if (selectedPoint == index) Color.Green else Color.Red,
                        radius = 10f,
                        center = pointOffset
                    )

                    // --------- una etiqueta cerca del punto selecc -------
                    if (selectedPoint == index) {
                        val moneda = exchangeRates[index]
                        val labelText = moneda.syncDate

                        drawContext.canvas.nativeCanvas.drawText(
                            labelText,
                            pointOffset.x + 20f,
                            pointOffset.y - 20f,
                            android.graphics.Paint().apply {
                                textSize = 35f
                                color = android.graphics.Color.BLACK
                                textAlign = android.graphics.Paint.Align.LEFT
                            }
                        )
                    }
                }
            }
        }

        //---------------------- info de un punto seleccionado debajo de la grafica -----------------
        selectedPoint?.let { index ->
            val moneda = exchangeRates[index]
            val labelText = "1 (base) = ${moneda.currencyCode}: ${moneda.exchangeRate}"

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Moneda: ${moneda.currencyCode} | Tasa: ${moneda.exchangeRate} | Fecha: ${moneda.syncDate}",
                        color = MaterialTheme.colorScheme.onSurface,
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = labelText,
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }

    }
}
