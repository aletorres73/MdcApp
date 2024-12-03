package com.mdcapp

import android.app.Application
import com.google.firebase.FirebaseApp
import com.mdcapp.di.initKoin
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.logger.Level

class OrdersApp: Application() {
    override fun onCreate() {
        super.onCreate()
        initKoin{
            androidLogger(Level.DEBUG)
            androidContext(this@OrdersApp)
        }
    }
}