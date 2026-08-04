package com.mdcapp.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mdcapp.domain.entities.toFormattedDate
import com.mdcapp.ui.viewmodels.SubscriptionViewModel
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubscriptionStatusScreen(
    vm: SubscriptionViewModel = koinViewModel(),
    onLogout: () -> Unit
) {
    val state by vm.uiState.collectAsState()
    val context = LocalContext.current

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        uri?.let {
            val inputStream = context.contentResolver.openInputStream(it)
            val bytes = inputStream?.readBytes()
            bytes?.let { b ->
                vm.uploadReceipt(b)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Estado de Suscripción") },
                actions = {
                    TextButton(onClick = onLogout) {
                        Text("Cerrar Sesión")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val isExpired =
                (state.userProfile?.subscriptionExpiresAt ?: 0L) < System.currentTimeMillis()
            val isManuallyEnabled = state.userProfile?.isManuallyEnabled == true

            if (isManuallyEnabled || !isExpired) {
                StatusCard(
                    title = "Suscripción Activa",
                    subtitle = "Vence el: ${state.userProfile?.subscriptionExpiresAt?.toFormattedDate()}",
                    icon = Icons.Default.CheckCircle,
                    color = Color(0xFF2E7D32)
                )
            } else {
                StatusCard(
                    title = "Suscripción Vencida",
                    subtitle = "Para seguir usando la app, por favor realiza el pago.",
                    icon = Icons.Default.Warning,
                    color = MaterialTheme.colorScheme.error
                )

                Spacer(modifier = Modifier.height(24.dp))

                PaymentInstructionsCard()

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        launcher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !state.isUploading
                ) {
                    if (state.isUploading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = Color.White
                        )
                    } else {
                        Text("Subir Comprobante de Pago")
                    }
                }

                if (state.uploadSuccess) {
                    Text(
                        "¡Comprobante subido! Tu cuenta se activará en breve.",
                        color = Color(0xFF2E7D32),
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                state.error?.let {
                    Text("Error: $it", color = MaterialTheme.colorScheme.error)
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                "Historial de Pagos",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.align(Alignment.Start)
            )

            LazyColumn(modifier = Modifier.fillMaxWidth()) {
                items(state.userProfile?.paymentHistory?.reversed() ?: emptyList()) { payment ->
                    ListItem(
                        headlineContent = { Text("Pago del ${payment.date.toFormattedDate()}") },
                        supportingContent = { Text("Estado: ${payment.status}") },
                        trailingContent = { Text(payment.amount.toString()) }
                    )
                    HorizontalDivider()
                }
            }
        }
    }
}

@Composable
fun StatusCard(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.1f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(48.dp))
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(title, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = color)
                Text(subtitle, fontSize = 14.sp)
            }
        }
    }
}

@Composable
fun PaymentInstructionsCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Info, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Datos para Transferencia", fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text("Alias: MDCAPP.PAGOS", fontSize = 16.sp)
            Text("CBU: 0000003100012345678901", fontSize = 16.sp)
            Text("Titular: MDC App Services", fontSize = 14.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "Una vez realizada la transferencia, sube la captura del comprobante aquí abajo.",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}
