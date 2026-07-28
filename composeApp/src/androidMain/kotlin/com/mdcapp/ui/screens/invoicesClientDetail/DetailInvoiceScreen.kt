package com.mdcapp.ui.screens.invoicesClientDetail

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mdcapp.domain.entities.BillingComments
import com.mdcapp.domain.entities.PaymentRegisterModel
import com.mdcapp.domain.entities.toFormattedDate
import com.mdcapp.domain.entities.toPrint
import com.mdcapp.ui.composables.common.DatePicker
import com.mdcapp.ui.screens.orders.OrderCard
import com.mdcapp.ui.viewmodels.invoices.DetailInvoiceViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun DetailInvoiceScreen(
    vm: DetailInvoiceViewModel,
    modifier: Modifier = Modifier,
    onBack: () -> Unit = {}
) {
    val state by vm.state.collectAsState()
    val buyOrder = state.buyOrder

    var showSheet by remember { mutableStateOf(false) }
    var showOrder by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showPaymentDialog by remember { mutableStateOf(false) }
    var showCommentDialog by remember { mutableStateOf(false) }
    var editingPayment by remember { mutableStateOf<PaymentRegisterModel?>(null) }
    var paymentAmount by remember { mutableStateOf("") }
    var paymentNotes by remember { mutableStateOf("") }
    var paymentMethod by remember { mutableStateOf("EFECTIVO") }
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
            onDateSelected = { millis ->
                vm.updateDeliveryDate(millis)
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

            // Sugerencia de Descuento
            val hasProntoPago = state.payments.any { it.method == "PRONTO_PAGO" }
            if (state.billing.expectedDiscount > 0 && !hasProntoPago) {
                val discountValue = state.billing.total * state.billing.expectedDiscount
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Descuento Sugerido", style = MaterialTheme.typography.titleSmall)
                            Text(
                                "Se sugiere aplicar un pronto pago de ${discountValue.toPrint()}",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                        TextButton(onClick = {
                            vm.registerMovement(
                                amount = discountValue,
                                notes = "Aplicado según condición: ${state.billing.paymentCondition}",
                                method = "PRONTO_PAGO",
                                isVirtual = true
                            )
                        }) {
                            Text("APLICAR")
                        }
                    }
                }
            }

            OrderCard(
                order = buyOrder,
                expanded = showOrder,
                onToggle = { showOrder = !showOrder }
            )

            // Nueva sección de Pagos
            PaymentsSection(
                payments = state.payments,
                onAddPayment = {
                    editingPayment = null
                    paymentAmount = ""
                    paymentNotes = ""
                    paymentMethod = "EFECTIVO"
                    showPaymentDialog = true
                },
                onEditPayment = { payment ->
                    editingPayment = payment
                    paymentAmount = payment.total.toString()
                    paymentNotes = payment.notes
                    paymentMethod = payment.method
                    showPaymentDialog = true
                },
                onReconcile = { vm.reconcileMovement(it) }
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
        val methods =
            listOf("EFECTIVO", "TRANSFERENCIA", "PRONTO_PAGO", "NOTA_CREDITO", "DESCUENTO_EXTRA")

        AlertDialog(
            onDismissRequest = {
                if (!state.isProcessingPayment) {
                    showPaymentDialog = false
                    editingPayment = null
                }
            },
            title = { Text(if (editingPayment == null) "Registrar Movimiento" else "Editar Movimiento") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    val displayBalance =
                        if (editingPayment == null) state.billing.rest else state.billing.rest + (editingPayment?.total
                            ?: 0.0)
                    Text(
                        "Saldo pendiente: $ $displayBalance",
                        style = MaterialTheme.typography.bodyMedium
                    )

                    Text("Tipo de Movimiento", style = MaterialTheme.typography.labelSmall)
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        methods.forEach { m ->
                            FilterChip(
                                selected = paymentMethod == m,
                                onClick = { paymentMethod = m },
                                label = { Text(m, style = MaterialTheme.typography.labelSmall) }
                            )
                        }
                    }

                    OutlinedTextField(
                        value = paymentAmount,
                        onValueChange = { paymentAmount = it },
                        label = { Text("Monto") },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !state.isProcessingPayment,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        prefix = { Text("$ ") }
                    )
                    OutlinedTextField(
                        value = paymentNotes,
                        onValueChange = { paymentNotes = it },
                        label = { Text("Notas / Referencia") },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !state.isProcessingPayment,
                        placeholder = { Text("Ej: Efectivo, Transf #1234, Recibo #55") },
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val amount = paymentAmount.toDoubleOrNull() ?: 0.0
                        if (amount > 0) {
                            if (editingPayment == null) {
                                val isVirtual =
                                    paymentMethod != "EFECTIVO" && paymentMethod != "TRANSFERENCIA"
                                vm.registerMovement(amount, paymentNotes, paymentMethod, isVirtual)
                            } else {
                                vm.editPayment(editingPayment!!, amount, paymentNotes)
                            }

                            if (!state.isProcessingPayment) {
                                showPaymentDialog = false
                                paymentAmount = ""
                                paymentNotes = ""
                                editingPayment = null
                            }
                        }
                    },
                    enabled = !state.isProcessingPayment
                ) {
                    Text(if (state.isProcessingPayment) "Procesando..." else "Confirmar")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showPaymentDialog = false
                        editingPayment = null
                    },
                    enabled = !state.isProcessingPayment
                ) {
                    Text("Cancelar")
                }
            }
        )
    }

    // Cerrar el diálogo cuando el pago se registra/edita con éxito
    LaunchedEffect(state.isProcessingPayment) {
        if (!state.isProcessingPayment && paymentAmount.isEmpty()) {
            showPaymentDialog = false
            editingPayment = null
        }
    }
}

@Composable
fun PaymentsSection(
    payments: List<PaymentRegisterModel>,
    onAddPayment: () -> Unit,
    onEditPayment: (PaymentRegisterModel) -> Unit,
    onReconcile: (PaymentRegisterModel) -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Historial de Movimientos", style = MaterialTheme.typography.titleMedium)
                TextButton(onClick = onAddPayment) { Text("+ Movimiento") }
            }
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            if (payments.isEmpty()) {
                Text("No hay movimientos registrados.", style = MaterialTheme.typography.bodySmall)
            } else {
                payments.forEach { payment ->
                    Column(modifier = Modifier.padding(vertical = 4.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        "ID: ${payment.id} - ${payment.date.toFormattedDate()}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    StatusBadge(payment.status)
                                }
                                Text(
                                    "${payment.method}${if (payment.notes.isNotBlank()) " - ${payment.notes}" else ""}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (payment.isVirtual) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    "$ ${payment.total}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                                    color = if (payment.isVirtual) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.onSurface
                                )
                                if (payment.status == "PENDIENTE_FABRICA") {
                                    IconButton(onClick = { onReconcile(payment) }) {
                                        Icon(
                                            Icons.Default.CheckCircle,
                                            contentDescription = "Conciliar",
                                            modifier = Modifier.size(20.dp),
                                            tint = Color(0xFF2E7D32)
                                        )
                                    }
                                }
                                IconButton(onClick = { onEditPayment(payment) }) {
                                    Icon(
                                        imageVector = Icons.Default.Edit,
                                        contentDescription = "Editar",
                                        modifier = Modifier.size(18.dp),
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
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

@Composable
fun StatusBadge(status: String) {
    val text: String
    val color: Color

    when (status) {
        "PENDIENTE_FABRICA" -> {
            text = "Pendiente Fábrica"
            color = Color(0xFFF9A825)
        }

        "IMPUTADO_FABRICA" -> {
            text = "Imputado"
            color = Color(0xFF2E7D32)
        }

        else -> {
            text = status
            color = Color.Gray
        }
    }

    Surface(
        color = color.copy(alpha = 0.1f),
        shape = MaterialTheme.shapes.extraSmall,
        border = androidx.compose.foundation.BorderStroke(0.5.dp, color.copy(alpha = 0.5f))
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
            style = MaterialTheme.typography.labelSmall,
            color = color,
            fontSize = 9.sp
        )
    }
}

@Composable
fun CommentsSection(
    comments: List<BillingComments>,
    onAddComment: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
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
                            comment.date.toFormattedDate(),
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

