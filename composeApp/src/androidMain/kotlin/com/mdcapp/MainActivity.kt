package com.mdcapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import com.google.firebase.FirebaseApp
import com.mdcapp.domain.remote.OrderRepository
import com.mdcapp.domain.service.OrderService
import com.mdcapp.ui.viewmodels.orders.OrdersScreen
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseApp.initializeApp(this)

        setContent {
            OrdersScreen()
            /*val scope = rememberCoroutineScope()
            val service = OrderRepository(OrderService())
            LaunchedEffect(Unit) {
                scope.launch {
                    println(
                        "MainAndroid: ${
                            service.getAllOrders().toList().flatten()
                        }"
                    )
                }
            }*/
        }
    }
}