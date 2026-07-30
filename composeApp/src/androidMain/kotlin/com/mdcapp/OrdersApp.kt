package com.mdcapp

import android.app.Application
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.mdcapp.di.initKoin
import com.mdcapp.domain.worker.BillingNotificationWorker
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.logger.Level
import java.util.concurrent.TimeUnit

class OrdersApp: Application() {
    override fun onCreate() {
        super.onCreate()
        initKoin{
            androidLogger(Level.DEBUG)
            androidContext(this@OrdersApp)
        }
        scheduleNotifications()
    }

    private fun scheduleNotifications() {
        val workRequest = PeriodicWorkRequestBuilder<BillingNotificationWorker>(
            24, TimeUnit.HOURS
        ).build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "BillingNotifications",
            ExistingPeriodicWorkPolicy.KEEP,
            workRequest
        )
    }
}
