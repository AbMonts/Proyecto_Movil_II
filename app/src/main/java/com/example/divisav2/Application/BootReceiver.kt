package com.example.divisav2.Application

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.divisav2.Workers.SyncExchangeWorker
import java.util.concurrent.TimeUnit

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action == Intent.ACTION_BOOT_COMPLETED) {
            context?.let {
                val workRequest = PeriodicWorkRequestBuilder<SyncExchangeWorker>(
                    1, TimeUnit.HOURS
                ).build()

                WorkManager.getInstance(it).enqueueUniquePeriodicWork(
                    "HourlySyncWorker",
                    ExistingPeriodicWorkPolicy.KEEP, // Evita duplicados
                    workRequest
                )
            }
        }
    }
}
