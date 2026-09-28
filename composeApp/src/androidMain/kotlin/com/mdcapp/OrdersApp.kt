package com.mdcapp

import android.app.Activity
import android.app.Application
import android.os.Bundle
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.mdcapp.di.initFirebaseApp
import com.mdcapp.di.initKoin
import com.mdcapp.domain.worker.BillingNotificationWorker
import com.mdcapp.ui.utils.CrashlyticsAntilog
import io.github.aakira.napier.DebugAntilog
import io.github.aakira.napier.Napier
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.logger.Level
import java.lang.ref.WeakReference
import java.util.concurrent.TimeUnit

class OrdersApp : Application() {

    companion object {
        private var currentActivityRef: WeakReference<Activity>? = null

        fun getCurrentActivity(): Activity? = currentActivityRef?.get()
    }

    override fun onCreate() {
        super.onCreate()
        println("🔑 [MDC_SESSION] OrdersApp.onCreate() started")

        registerActivityLifecycleCallbacks(object : ActivityLifecycleCallbacks {
            override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
                println("🔑 [MDC_SESSION] Activity created: ${activity.javaClass.simpleName}")
            }

            override fun onActivityStarted(activity: Activity) {
                println("🔑 [MDC_SESSION] Activity started: ${activity.javaClass.simpleName}")
            }

            override fun onActivityResumed(activity: Activity) {
                println("🔑 [MDC_SESSION] Activity resumed: ${activity.javaClass.simpleName}")
                currentActivityRef = WeakReference(activity)
            }

            override fun onActivityPaused(activity: Activity) {
                println("🔑 [MDC_SESSION] Activity paused: ${activity.javaClass.simpleName}")
                if (currentActivityRef?.get() == activity) {
                    currentActivityRef = null
                }
            }

            override fun onActivityStopped(activity: Activity) {
                println("🔑 [MDC_SESSION] Activity stopped: ${activity.javaClass.simpleName}")
            }

            override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}
            override fun onActivityDestroyed(activity: Activity) {
                println("🔑 [MDC_SESSION] Activity destroyed: ${activity.javaClass.simpleName}")
            }
        })

        if (BuildConfig.DEBUG) {
            Napier.base(DebugAntilog())
        } else {
            Napier.base(CrashlyticsAntilog())
        }

        inspectSharedPreferences()

        initFirebaseApp(this)
        initKoin {
            androidLogger(Level.DEBUG)
            androidContext(this@OrdersApp)
        }
        scheduleNotifications()
        println("🔑 [MDC_SESSION] OrdersApp.onCreate() completed")
    }

    private fun inspectSharedPreferences() {
        try {
            val prefsDir = java.io.File(applicationInfo.dataDir, "shared_prefs")
            if (prefsDir.exists() && prefsDir.isDirectory) {
                val files = prefsDir.listFiles()
                println("🔑 [MDC_SESSION] SharedPreferences directory found with ${files?.size ?: 0} files:")
                files?.forEach { file ->
                    println("  - ${file.name} (${file.length()} bytes)")
                }
            } else {
                println("⚠️ [MDC_SESSION] SharedPreferences directory does NOT exist yet at: ${prefsDir.absolutePath}")
            }
        } catch (e: Exception) {
            println("❌ [MDC_SESSION] Error inspecting SharedPreferences: ${e.message}")
        }
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
