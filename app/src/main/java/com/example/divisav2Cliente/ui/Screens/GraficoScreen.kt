package com.example.divisav2Cliente.ui.Screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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


@Composable
fun LineChartCompose(exchangeRates: List<Moneda>) {
    val chartData = exchangeRates.mapIndexed { index, moneda ->
        index.toFloat() to moneda.exchangeRate.toFloat()
    }

    Box(modifier = Modifier.fillMaxSize()) {
        androidx.compose.foundation.Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
        ) {
            val path = Path().apply {
                moveTo(chartData.first().first * 100, chartData.first().second * 10)
                for (point in chartData.drop(1)) {
                    lineTo(point.first * 100, point.second * 10)
                }
            }

            drawPath(
                path = path,
                color = Color.Blue,
                style = Stroke(width = 4f)
            )

            chartData.forEach { point ->
                drawCircle(
                    color = Color.Red,
                    radius = 6f,
                    center = Offset(point.first * 100, point.second * 10)
                )
            }
        }
    }
}

