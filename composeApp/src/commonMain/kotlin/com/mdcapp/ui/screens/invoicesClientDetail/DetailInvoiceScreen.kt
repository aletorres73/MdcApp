package com.mdcapp.ui.screens.invoicesClientDetail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
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
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mdcapp.domain.entities.BillingComments
import com.mdcapp.domain.entities.BuyOrderModel
import com.mdcapp.domain.entities.FactoryModel
import com.mdcapp.domain.entities.MovementMethod
import com.mdcapp.domain.entities.MovementStatus
import com.mdcapp.domain.entities.PaymentRegisterModel
import com.mdcapp.domain.entities.toFormattedDate
import com.mdcapp.domain.entities.toPrint
import com.mdcapp.ui.composables.common.DatePicker
import com.mdcapp.ui.composables.common.LoadingOverlay
import com.mdcapp.ui.screens.orders.OrderCard
import com.mdcapp.ui.utils.AppBackHandler
import com.mdcapp.ui.viewmodels.invoices.DetailInvoiceViewModel
import kotlinx.coroutines.launch

fun String.toColor(): Color {
    val hex = this.removePrefix("#")
    return Color(hex.toLong(16) or 0xFF000000)
}

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
    var paymentMethod by remember { mutableStateOf(MovementMethod.EFECTIVO) }
    var commentText by remember { mutableStateOf("") }
    var showStateDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    var editNumber by remember { mutableStateOf("") }
    var editType by remember { mutableStateOf("") }
    var editTotal by remember { mutableStateOf("") }
    var editBrand by remember { mutableStateOf("") }
    var editBranch by remember { mutableStateOf("") }
    var editLoadDate by remember { mutableStateOf(0L) }
    var editDeliveryDate by remember { mutableStateOf(0L) }
    var editOrderId by remember { mutableStateOf("") }
    var showEditLoadDatePicker by remember { mutableStateOf(false) }
    var showEditDeliveryDatePicker by remember { mutableStateOf(false) }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)
    val scope = rememberCoroutineScope()
    val snackBarHostState = remember { SnackbarHostState() }

    AppBackHandler { onBack() }

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

    LaunchedEffect(state.isDeleted) {
        if (state.isDeleted) {
            onBack()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackBarHostState) },
        topBar = {
            InvoiceHeaderTopBar(
                billing = state.billing,
                onBack = onBack,
                onEdit = {
                    editNumber = state.billing.billingNumber
                    editType = state.billing.type.ifEmpty { "Factura" }
                    editTotal = if (state.billing.total > 0) state.billing.total.toString() else ""
                    editBrand = state.selectedFactory?.name ?: state.billing.brand
                    editBranch = state.selectedBranch.ifEmpty { state.billing.branch }
                    editLoadDate = state.billing.loadDate
                    editDeliveryDate = state.billing.deliveryDate
                    editOrderId = state.billing.orderId
                    showEditDialog = true
                },
                onDelete = {
                    if (state.payments.isEmpty()) {
                        showDeleteDialog = true
                    } else {
                        scope.launch {
                            snackBarHostState.showSnackbar(
                                "No se puede eliminar: tiene movimientos",
                                duration = SnackbarDuration.Short
                            )
                        }
                    }
                }
            )
        }
    ) { padding ->
        if (state.error != null) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(state.error!!, color = MaterialTheme.colorScheme.error)
                    Spacer(Modifier.height(16.dp))
                    Button(onClick = onBack) { Text("Volver") }
                }
            }
        } else {
            Column(
                modifier = modifier
                    .padding(padding)
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 4.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                if (state.billing.documentUrl.isNotEmpty()) {
                    val uriHandler = LocalUriHandler.current
                    AssistChip(
                        onClick = { vm.openDocument(uriHandler) },
                        label = { Text("Ver Documento Adjunto") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                DatesCard(billing = state.billing) { showDatePicker = true }

                TotalsCard(billing = state.billing)

                if (state.billing.orderId.isEmpty()) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.5f)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(Modifier.width(12.dp))
                            Text(
                                "Esta factura fue creada sin un pedido previo",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                PaymentConditionCard(billing = state.billing) { showSheet = true }

                // Sugerencia de Descuento
                val hasProntoPago =
                    state.payments.any { it.method == MovementMethod.PRONTO_PAGO.name }
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
                                Text(
                                    "Descuento Sugerido",
                                    style = MaterialTheme.typography.titleSmall
                                )
                                Text(
                                    "Se sugiere aplicar un pronto pago de ${discountValue.toPrint()}",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                            TextButton(onClick = {
                                vm.registerMovement(
                                    amount = discountValue,
                                    notes = "Aplicado según condición: ${state.billing.paymentCondition}",
                                    method = MovementMethod.PRONTO_PAGO
                                )
                            }) {
                                Text("APLICAR")
                            }
                        }
                    }
                }

                if (state.billing.orderId.isNotEmpty()) {
                    OrderCard(
                        order = buyOrder,
                        expanded = showOrder,
                        onToggle = { showOrder = !showOrder }
                    )
                }

                // Nueva sección de Pagos
                PaymentsSection(
                    payments = state.payments,
                    onAddPayment = {
                        editingPayment = null
                        paymentAmount = ""
                        paymentNotes = ""
                        paymentMethod = MovementMethod.EFECTIVO
                        showPaymentDialog = true
                    },
                    onEditPayment = { payment ->
                        editingPayment = payment
                        paymentAmount = payment.total.toString()
                        paymentNotes = payment.notes
                        paymentMethod = MovementMethod.fromName(payment.method)
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
                val states = listOf("Pendiente", "Cobrado", "Devuelta", "Cerrada")
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

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Eliminar Factura") },
            text = { Text("¿Está seguro que desea eliminar la factura ${state.billing.billingNumber}? Esta acción no se puede deshacer.") },
            confirmButton = {
                Button(
                    onClick = {
                        vm.deleteInvoice()
                        showDeleteDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Eliminar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    if (showEditLoadDatePicker) {
        DatePicker(
            onDismissRequest = { showEditLoadDatePicker = false },
            onDateSelected = { millis ->
                editLoadDate = millis
                showEditLoadDatePicker = false
            },
            onDismissButton = { showEditLoadDatePicker = false },
            enable = true
        )
    }

    if (showEditDeliveryDatePicker) {
        DatePicker(
            onDismissRequest = { showEditDeliveryDatePicker = false },
            onDateSelected = { millis ->
                editDeliveryDate = millis
                showEditDeliveryDatePicker = false
            },
            onDismissButton = { showEditDeliveryDatePicker = false },
            enable = true
        )
    }

    val editSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    if (showEditDialog) {
        ModalBottomSheet(
            onDismissRequest = { showEditDialog = false },
            sheetState = editSheetState,
            modifier = Modifier.fillMaxHeight()
        ) {
            EditInvoiceSheetContent(
                state = state,
                editNumber = editNumber,
                onNumberChange = { editNumber = it },
                editType = editType,
                onTypeChange = { editType = it },
                editTotal = editTotal,
                onTotalChange = { editTotal = it },
                editBrand = editBrand,
                onBrandChange = { editBrand = it },
                editBranch = editBranch,
                onBranchChange = { editBranch = it },
                editOrderId = editOrderId,
                onOrderSelected = { selectedOrder ->
                    if (selectedOrder == null) {
                        editOrderId = ""
                    } else {
                        editOrderId = selectedOrder.id
                        editBrand = selectedOrder.factory
                        editBranch = selectedOrder.branch
                        val factory = state.factories.find {
                            it.name.equals(
                                selectedOrder.factory,
                                ignoreCase = true
                            )
                        }
                        if (factory != null) {
                            vm.onFactorySelected(factory)
                        }
                    }
                },
                editLoadDate = editLoadDate,
                onShowLoadDatePicker = { showEditLoadDatePicker = true },
                editDeliveryDate = editDeliveryDate,
                onShowDeliveryDatePicker = { showEditDeliveryDatePicker = true },
                onFactorySelected = { factory ->
                    editBrand = factory.name
                    editBranch = factory.branchList.firstOrNull() ?: ""
                    vm.onFactorySelected(factory)
                },
                onBranchSelected = { branch ->
                    editBranch = branch
                    vm.onBranchSelected(branch)
                },
                onSave = {
                    val totalDouble = editTotal.toDoubleOrNull() ?: 0.0
                    vm.updateInvoiceDetails(
                        newBillingNumber = editNumber,
                        newType = editType,
                        newTotal = totalDouble,
                        newBrand = editBrand,
                        newBranch = editBranch,
                        newLoadDate = editLoadDate,
                        newDeliveryDate = editDeliveryDate,
                        newOrderId = editOrderId
                    )
                    showEditDialog = false
                },
                onCancel = { showEditDialog = false }
            )
        }
    }

    if (showPaymentDialog) {
        val methods = MovementMethod.entries.toTypedArray()

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
                                label = {
                                    Text(
                                        m.displayName,
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                }
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
                        placeholder = { Text("Ej: Efectivo, Transf N° 1234, Recibo N° 55") },
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
                                vm.registerMovement(amount, paymentNotes, paymentMethod)
                            } else {
                                vm.editPayment(
                                    editingPayment!!,
                                    amount,
                                    paymentNotes,
                                    paymentMethod
                                )
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

    LoadingOverlay(state.isLoading || state.isProcessingPayment)
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
                                        "N° ${payment.id} - ${payment.date.toFormattedDate()}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    StatusBadge(payment.status)
                                }
                                val methodEnum = MovementMethod.fromName(payment.method)
                                Text(
                                    "${methodEnum.displayName}${if (payment.notes.isNotBlank()) " - ${payment.notes}" else ""}",
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
                                if (payment.status == MovementStatus.PENDIENTE.name) {
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
    val statusEnum = MovementStatus.fromName(status)
    val color = statusEnum.colorHex.toColor()

    Surface(
        color = color.copy(alpha = 0.1f),
        shape = MaterialTheme.shapes.extraSmall,
        border = androidx.compose.foundation.BorderStroke(0.5.dp, color.copy(alpha = 0.5f))
    ) {
        Text(
            text = statusEnum.displayName,
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FactorySelectorDropdown(
    factories: List<FactoryModel>,
    selectedFactory: FactoryModel?,
    onFactorySelected: (FactoryModel) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it }
    ) {
        OutlinedTextField(
            value = selectedFactory?.name ?: "Seleccionar Fábrica",
            onValueChange = {},
            readOnly = true,
            label = { Text("Fábrica / Marca") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable, true).fillMaxWidth()
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            factories.forEach { factory ->
                DropdownMenuItem(
                    text = { Text(factory.name) },
                    onClick = {
                        onFactorySelected(factory)
                        expanded = false
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SegmentSelectorDropdown(
    segments: List<String>,
    selectedSegment: String,
    onSegmentSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it }
    ) {
        OutlinedTextField(
            value = selectedSegment.ifEmpty { "Seleccionar Segmento" },
            onValueChange = {},
            readOnly = true,
            label = { Text("Segmentación / Sucursal") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable, true).fillMaxWidth()
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            segments.forEach { segment ->
                DropdownMenuItem(
                    text = { Text(segment) },
                    onClick = {
                        onSegmentSelected(segment)
                        expanded = false
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderSelectorDropdown(
    orders: List<BuyOrderModel>,
    selectedOrderId: String,
    onOrderSelected: (BuyOrderModel?) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedOrder = orders.find { it.id == selectedOrderId }

    val displayText = when {
        selectedOrder != null -> "Pedido Nº ${selectedOrder.id} - ${selectedOrder.factory}"
        selectedOrderId.isNotEmpty() -> "Pedido Nº $selectedOrderId"
        else -> "Sin Pedido (Facturación Directa)"
    }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = displayText,
            onValueChange = {},
            readOnly = true,
            label = { Text("Pedido Asignado") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable, true).fillMaxWidth()
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            DropdownMenuItem(
                text = {
                    Text(
                        "Sin Pedido (Facturación Directa)",
                        style = MaterialTheme.typography.bodyMedium
                    )
                },
                onClick = {
                    onOrderSelected(null)
                    expanded = false
                }
            )
            orders.forEach { order ->
                DropdownMenuItem(
                    text = {
                        Column {
                            Text(
                                "Pedido Nº ${order.id}",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                "${order.factory} - ${order.branch}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    },
                    onClick = {
                        onOrderSelected(order)
                        expanded = false
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun EditInvoiceSheetContent(
    state: DetailInvoiceViewModel.UiState,
    editNumber: String,
    onNumberChange: (String) -> Unit,
    editType: String,
    onTypeChange: (String) -> Unit,
    editTotal: String,
    onTotalChange: (String) -> Unit,
    editBrand: String,
    onBrandChange: (String) -> Unit,
    editBranch: String,
    onBranchChange: (String) -> Unit,
    editOrderId: String,
    onOrderSelected: (BuyOrderModel?) -> Unit,
    editLoadDate: Long,
    onShowLoadDatePicker: () -> Unit,
    editDeliveryDate: Long,
    onShowDeliveryDatePicker: () -> Unit,
    onFactorySelected: (FactoryModel) -> Unit,
    onBranchSelected: (String) -> Unit,
    onSave: () -> Unit,
    onCancel: () -> Unit
) {
    val documentTypes =
        listOf("Factura", "Remito", "Factura A", "Factura B", "Presupuesto", "Nota de Crédito")

    val currentFactory =
        state.factories.find { it.name.equals(editBrand, ignoreCase = true) }
            ?: state.selectedFactory

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Encabezado
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("Editar Comprobante", style = MaterialTheme.typography.titleLarge)
                Text(
                    "Modifique los datos del comprobante",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(onClick = onCancel) {
                Icon(Icons.Default.Close, contentDescription = "Cerrar")
            }
        }

        // Sección 1: Datos del Comprobante
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(
                    alpha = 0.4f
                )
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("Tipo y Monto", style = MaterialTheme.typography.titleMedium)

                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    documentTypes.forEach { type ->
                        FilterChip(
                            selected = editType == type,
                            onClick = { onTypeChange(type) },
                            label = { Text(type, style = MaterialTheme.typography.labelSmall) }
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = editNumber,
                        onValueChange = onNumberChange,
                        label = { Text("N° Comprobante") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = editTotal,
                        onValueChange = onTotalChange,
                        label = { Text("Monto Total") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        prefix = { Text("$ ") }
                    )
                }
            }
        }

        // Sección 2: Pedido y Origen
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(
                    alpha = 0.4f
                )
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("Pedido y Origen", style = MaterialTheme.typography.titleMedium)

                OrderSelectorDropdown(
                    orders = state.clientOrders,
                    selectedOrderId = editOrderId,
                    onOrderSelected = onOrderSelected,
                    modifier = Modifier.fillMaxWidth()
                )

                if (state.factories.isNotEmpty()) {
                    FactorySelectorDropdown(
                        factories = state.factories,
                        selectedFactory = currentFactory,
                        onFactorySelected = onFactorySelected
                    )

                    if (currentFactory != null && currentFactory.branchList.isNotEmpty()) {
                        SegmentSelectorDropdown(
                            segments = currentFactory.branchList,
                            selectedSegment = editBranch,
                            onSegmentSelected = onBranchSelected
                        )
                    }
                } else {
                    OutlinedTextField(
                        value = editBrand,
                        onValueChange = onBrandChange,
                        label = { Text("Fábrica / Marca") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    if (editBranch.isNotEmpty()) {
                        OutlinedTextField(
                            value = editBranch,
                            onValueChange = onBranchChange,
                            label = { Text("Sucursal / Segmento") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                    }
                }
            }
        }

        // Sección 3: Fechas
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(
                    alpha = 0.4f
                )
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("Fechas", style = MaterialTheme.typography.titleMedium)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Fecha Emisión", style = MaterialTheme.typography.labelSmall)
                        Text(
                            if (editLoadDate > 0) editLoadDate.toFormattedDate() else "Sin fecha",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    TextButton(onClick = onShowLoadDatePicker) {
                        Text("Cambiar")
                    }
                }

                HorizontalDivider(thickness = 0.5.dp)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Fecha Vencimiento", style = MaterialTheme.typography.labelSmall)
                        Text(
                            if (editDeliveryDate > 0) editDeliveryDate.toFormattedDate() else "Sin fecha",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    TextButton(onClick = onShowDeliveryDatePicker) {
                        Text("Cambiar")
                    }
                }
            }
        }

        // Acciones
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp, bottom = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = onCancel,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.outlinedButtonColors()
            ) {
                Text("Cancelar")
            }

            Button(
                onClick = onSave,
                modifier = Modifier.weight(1f)
            ) {
                Text("Guardar Cambios")
            }
        }
    }
}
