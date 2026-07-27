package com.mdcapp.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.mdcapp.ui.composables.detailorders.OrderDetailInfo
import com.mdcapp.ui.viewmodels.buyorders.BuyOrdersViewModel
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.annotation.KoinExperimentalAPI

@OptIn(ExperimentalMaterial3Api::class, KoinExperimentalAPI::class)
@Composable
fun OrderDetailScreen(
    clientId: String,
    orderId: String,
    factoryName: String,
    onBack: () -> Unit,
    viewModel: BuyOrdersViewModel = koinViewModel()
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detalle de Pedido #$orderId") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Atrás")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            OrderDetailInfo(
                clientId = clientId,
                orderId = orderId,
                factoryName = factoryName,
                vm = viewModel,
                onBillingClicked = { /* Navigate to individual billing edit if needed */ }
            )
        }
    }
}

