package com.mdcapp.ui.screens

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
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mdcapp.domain.entities.PaymentInfo
import com.mdcapp.domain.entities.toFormattedDate
import com.mdcapp.domain.entities.toPrint
import com.mdcapp.ui.viewmodels.SubscriptionViewModel
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.annotation.KoinExperimentalAPI

@OptIn(ExperimentalMaterial3Api::class, KoinExperimentalAPI::class)
@Composable
fun SubscriptionStatusScreen(
    vm: SubscriptionViewModel = koinViewModel(),
    onLogout: () -> Unit
) {
    val state by vm.uiState.collectAsState()

    // TODO: Implement multiplatform file picker
    /*val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        uri?.let {
            val inputStream = context.contentResolver.openInputStream(it)
            val bytes = inputStream?.readBytes()
            bytes?.let { b ->
                vm.uploadReceipt(b)
            }
        }
    }*/

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
                    title = "Suscripción Inactiva",
                    subtitle = "Para seguir usando la app, por favor realiza el pago.",
                    icon = Icons.Default.Warning,
                    color = MaterialTheme.colorScheme.error
                )

                Spacer(modifier = Modifier.height(24.dp))

                PaymentInstructionsCard(state.paymentInfo)

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        // TODO: launcher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !state.isUploading && false // Disabled for now until multiplatform picker is implemented
                ) {
                    if (state.isUploading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = Color.White
                        )
                    } else {
                        Text("Subir Comprobante de Pago (Android only for now)")
                    }
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
                        headlineContent = {
                            val title =
                                if (payment.paymentId > 0) "Pago N° ${payment.paymentId}" else "Pago (Procesando ID)"
                            Text("$title del ${payment.date.toFormattedDate()}")
                        },
                        supportingContent = {
                            Text("Estado: ${payment.status}")
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
fun PaymentInstructionsCard(info: PaymentInfo) {
    val clipboardManager = LocalClipboardManager.current

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

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Alias: ${info.alias}", fontSize = 16.sp, modifier = Modifier.weight(1f))
                TextButton(onClick = { clipboardManager.setText(AnnotatedString(info.alias)) }) {
                    Text("Copiar", fontSize = 12.sp)
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("CBU: ${info.cbu}", fontSize = 16.sp, modifier = Modifier.weight(1f))
                TextButton(onClick = { clipboardManager.setText(AnnotatedString(info.cbu)) }) {
                    Text("Copiar", fontSize = 12.sp)
                }
            }

            Text("Titular: ${info.titular}", fontSize = 14.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "Monto a transferir: ${info.amount.toPrint()}",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "Importante: El comprobante debe ser una captura de pantalla clara de la transferencia realizada.",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}
