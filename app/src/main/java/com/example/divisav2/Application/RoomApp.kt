package com.example.divisav2.Application

import android.app.Application
import androidx.work.Configuration
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.divisav2.Workers.SyncExchangeWorker
import dagger.hilt.android.HiltAndroidApp
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltAndroidApp
class RoomApp : Application(), Configuration.Provider {


    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()
        scheduleSyncWorker()
    }

    private fun scheduleSyncWorker() {
        val workRequest = PeriodicWorkRequestBuilder<SyncExchangeWorker>(1, TimeUnit.HOURS)
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED) // Solo si hay internet
                    .setRequiresBatteryNotLow(true) // Evita que se ejecute si la batería está baja
                    .setRequiresCharging(false) // No requiere que el dispositivo esté cargando
                    .setRequiresDeviceIdle(false) // Se ejecuta incluso si el dispositivo está en uso
                    .build()
            )
            .build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "SyncExchangeWorker",
            ExistingPeriodicWorkPolicy.UPDATE,
            workRequest
        )
    }

}