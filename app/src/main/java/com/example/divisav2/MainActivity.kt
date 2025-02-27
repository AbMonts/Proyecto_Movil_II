package com.example.divisav2

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.lifecycleScope
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.divisav2.APIService.ExchangeAPI
import com.example.divisav2.Application.RoomApp
import com.example.divisav2.Data.Entities.MonedaEntity
import com.example.divisav2.Data.Modelo.Moneda
import com.example.divisav2.Data.Repository.ExchangeRepository
import com.example.divisav2.ViewModel.MainViewModel
import com.example.divisav2.ViewModel.MainViewModelFactory
import com.example.divisav2.Workers.SyncExchangeWorker
import com.example.divisav2.ui.Screens.MainScreen
import com.example.divisav2.ui.theme.DivisaV2Theme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            DivisaV2Theme {
                MainScreen(viewModel)
            }
        }

    }




}
