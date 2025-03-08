package com.example.divisav2Cliente

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.divisav2Cliente.ui.Screens.ExchangeScreen
import com.example.divisav2Cliente.ui.Screens.GraficoScreen
import com.example.divisav2Cliente.ui.theme.DivisaV2Theme
import com.example.divisav2Cliente.ui.viewmodel.ExchangeViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val viewModel: ExchangeViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            DivisaV2Theme{
                //ExchangeScreen(viewModel)
                AppNavigation(viewModel)
            }
        }


        }

    @Composable
    fun AppNavigation(viewModel: ExchangeViewModel) {
        val navController = rememberNavController()

        NavHost(navController = navController, startDestination = "exchangeScreen") {
            composable("exchangeScreen") {
                ExchangeScreen(viewModel, navController)
            }
            composable("chartScreen/{moneda}") { backStackEntry ->
                val moneda = backStackEntry.arguments?.getString("moneda") ?: "MXN"
                GraficoScreen(viewModel, moneda, navController)
            }
        }
    }



}
