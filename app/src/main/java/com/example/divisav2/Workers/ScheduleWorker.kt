package com.example.divisav2.Workers


import android.app.Application
import androidx.work.*
import java.util.concurrent.TimeUnit

class ScheduleWorker : Application() {

    override fun onCreate() {
        super.onCreate()
        scheduleWorker()
    }

    private fun scheduleWorker() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val workRequest = PeriodicWorkRequestBuilder<SyncExchangeWorker>(1, TimeUnit.HOURS) 
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "SyncExchangeRates",
            ExistingPeriodicWorkPolicy.KEEP,
            workRequest
        )
    }
}
