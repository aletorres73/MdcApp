package com.mdcapp.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mdcapp.domain.entities.toFormattedDate
import com.mdcapp.ui.viewmodels.SubscriptionViewModel
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.annotation.KoinExperimentalAPI

@OptIn(ExperimentalMaterial3Api::class, KoinExperimentalAPI::class)
@Composable
fun PaymentHistoryScreen(
    vm: SubscriptionViewModel = koinViewModel(),
    onBack: () -> Unit
) {
    val state by vm.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Historial de Pagos") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            val history = state.userProfile?.paymentHistory?.reversed() ?: emptyList()

            if (history.isEmpty()) {
                Text(
                    "No hay pagos registrados.",
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.bodyLarge
                )
            } else {
                LazyColumn(modifier = Modifier.fillMaxWidth()) {
                    items(history) { payment ->
                        ListItem(
                            headlineContent = { Text("Pago del ${payment.date.toFormattedDate()}") },
                            supportingContent = {
                                Column {
                                    Text("Estado: ${payment.status}")
                                    if (payment.paymentInfoId.isNotEmpty()) {
                                        Text(
                                            "ID Cuenta: ${payment.paymentInfoId}",
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                    }
                                }
                            },
                            trailingContent = {
                                Text(
                                    "$%,.2f".format(payment.amount),
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        )
                        HorizontalDivider()
                    }
                }
            }
        }
    }
}
