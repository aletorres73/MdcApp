package com.mdcapp.ui.screens.invoicesClientDetail

import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mdcapp.data.model.formatter
import com.mdcapp.ui.composables.common.DatePicker
import com.mdcapp.ui.screens.orders.OrderCard
import com.mdcapp.ui.viewmodels.invoices.DetailInvoiceViewModel
import kotlinx.coroutines.launch

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailInvoiceScreen(
    vm: DetailInvoiceViewModel,
    modifier: Modifier = Modifier,
    onBack: () -> Unit = {}
) {
    val state by vm.state.collectAsState()
    val buyOrder = state.buyOrder

    var showSheet by remember { mutableStateOf(false) }
    var showArticles by remember { mutableStateOf(false) }
    var showOrder by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showPaymentDialog by remember { mutableStateOf(false) }
    var showCommentDialog by remember { mutableStateOf(false) }
    var paymentAmount by remember { mutableStateOf("") }
    var commentText by remember { mutableStateOf("") }
    var showStateDialog by remember { mutableStateOf(false) }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)
    val scope = rememberCoroutineScope()
    val snackBarHostState = remember { SnackbarHostState() }

    BackHandler { onBack() }

    if (showSheet) {
        ModalBottomSheet(
            onDismissRequest = { showSheet = false },
            sheetState = sheetState
        ) {
            PaymentConditionListSheet(
                list = state.paymentConditionList,
                onSelect = { condition ->
                    vm.updateSelectedPaymentCondition(condition)
                    showSheet = false
                }
            )
        }
    }

    if (showDatePicker) {
        DatePicker(
            onDismissRequest = { showDatePicker = false },
            onDateSelected = { onDateSelected ->
                vm.updateDeliveryDate(onDateSelected.formatter())
                showDatePicker = false
            },
            onDismissButton = { showDatePicker = false },
            enable = true
        )
    }

    LaunchedEffect(state.message) {
        state.message?.let { msg ->
            scope.launch {
                snackBarHostState.showSnackbar(msg, duration = SnackbarDuration.Short)
                vm.clearMessage()
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackBarHostState) },
        topBar = {
            InvoiceHeaderTopBar(
                billing = state.billing,
                onBack = onBack
            )
        },
        bottomBar = {
            if (state.billing.rest > 0) {
                BottomAppBar {
                    Button(
                        onClick = { showPaymentDialog = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                    ) {
                        Text("Registrar Pago")
                    }
                }
            }
        }
    ) { padding ->
        Column(
            modifier = modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            DatesCard(billing = state.billing) { showDatePicker = true }

            TotalsCard(billing = state.billing)

            /*            ArticlesCard(
                            articles = state.billing.articles,
                            expanded = showArticles,
                            onToggle = { showArticles = !showArticles }
                        )*/

            PaymentConditionCard(billing = state.billing) { showSheet = true }

            OrderCard(
                order = buyOrder,
                expanded = showOrder,
                onToggle = { showOrder = !showOrder }
            )

            // Nueva sección de Comentarios
            CommentsSection(
                comments = state.billing.comments,
                onAddComment = { showCommentDialog = true }
            )

            // Nueva sección de Cambio de Estado
            Button(
                onClick = { showStateDialog = true },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
            ) {
                Text("Cambiar Estado (${state.billing.stateBilling})")
            }
        }
    }

    if (showCommentDialog) {
        AlertDialog(
            onDismissRequest = { showCommentDialog = false },
            title = { Text("Agregar Comentario") },
            text = {
                OutlinedTextField(
                    value = commentText,
                    onValueChange = { commentText = it },
                    label = { Text("Nota") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )
            },
            confirmButton = {
                Button(onClick = {
                    if (commentText.isNotBlank()) {
                        vm.addComment(commentText)
                        commentText = ""
                        showCommentDialog = false
                    }
                }) { Text("Guardar") }
            },
            dismissButton = {
                TextButton(onClick = { showCommentDialog = false }) { Text("Cancelar") }
            }
        )
    }

    if (showStateDialog) {
        AlertDialog(
            onDismissRequest = { showStateDialog = false },
            title = { Text("Cambiar Estado") },
            text = {
                val states = listOf("Pendiente", "En proceso", "Cobrado", "Devuelta", "Cerrada")
                Column {
                    states.forEach { s ->
                        TextButton(
                            onClick = {
                                vm.updateState(s)
                                showStateDialog = false
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                s,
                                color = if (state.billing.stateBilling == s) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showStateDialog = false }) { Text("Cerrar") }
            }
        )
    }

    if (showPaymentDialog) {
        AlertDialog(
            onDismissRequest = { showPaymentDialog = false },
            title = { Text("Registrar Pago") },
            text = {
                Column {
                    Text("Saldo pendiente: $ ${state.billing.rest}")
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = paymentAmount,
                        onValueChange = { paymentAmount = it },
                        label = { Text("Monto a pagar") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val amount = paymentAmount.toDoubleOrNull() ?: 0.0
                        if (amount > 0) {
                            vm.registerPayment(amount)
                            showPaymentDialog = false
                            paymentAmount = ""
                        }
                    }
                ) {
                    Text("Confirmar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showPaymentDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
fun CommentsSection(
    comments: List<com.mdcapp.data.model.BillingComments>,
    onAddComment: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
            ) {
                Text("Seguimiento / Notas", style = MaterialTheme.typography.titleMedium)
                TextButton(onClick = onAddComment) { Text("+ Nota") }
            }
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            if (comments.isEmpty()) {
                Text("No hay notas registradas.", style = MaterialTheme.typography.bodySmall)
            } else {
                comments.forEach { comment ->
                    Column(modifier = Modifier.padding(vertical = 4.dp)) {
                        Text(
                            comment.date,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(comment.comments, style = MaterialTheme.typography.bodyMedium)
                    }
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 4.dp),
                        thickness = 0.5.dp
                    )
                }
            }
        }
    }
}
