package com.mdcapp.ui.composables.detailorders

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
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
import androidx.compose.ui.unit.dp
import com.mdcapp.domain.entities.BillingModel
import com.mdcapp.domain.entities.PaymentRegisterModel
import com.mdcapp.ui.composables.billings.BillingInputChip
import com.mdcapp.ui.composables.billings.BillingList
import com.mdcapp.ui.composables.billings.BottomSheetPaymentCondition
import com.mdcapp.ui.composables.billings.PaymentsRegister
import com.mdcapp.ui.composables.billings.formatValue
import com.mdcapp.ui.composables.buyorders.BuyOrderItem
import com.mdcapp.ui.composables.common.DatePicker
import com.mdcapp.ui.composables.common.LoadingIndicator
import com.mdcapp.ui.viewmodels.buyorders.BuyOrdersViewModel
import kotlinx.coroutines.launch


@Composable
fun OrderDetailInfo(
    clientId: String,
    orderId: String,
    factoryName: String,
    vm: BuyOrdersViewModel,
    onBillingClicked: (BillingModel) -> Unit
) {
    // Estado del ViewModel
    val state by vm.state.collectAsState()
    val tempState by vm.tempState.collectAsState()

    // Estados locales para controlar la visibilidad de los componentes
    var isBuyOrderClicked by remember { mutableStateOf(false) }
    var isBillingClicked by remember { mutableStateOf(false) }
    var isDateSelect by remember { mutableStateOf(false) }
    var isAddPaymentCondition by remember { mutableStateOf(false) }
    var isAddPaymentRegister by remember { mutableStateOf(false) }
    var isCheckPaymentRegister by remember { mutableStateOf(false) }
    var billingNumber by remember { mutableStateOf("") }

    // CoroutineScope para lanzar corrutinas
    val scope = rememberCoroutineScope()

    // Inicializar el ViewModel cuando se carga la pantalla
    LaunchedEffect(clientId, orderId, factoryName) {
        vm.init(clientId, orderId, factoryName)
    }

    // Mostrar indicador de carga si alguna operación está en progreso
    LoadingIndicator(
        enabled = state.loadingOrder || state.loadingBillings || state.loadingPayments || state.loadingPaymentsRegister
    )

    // BottomSheet para mostrar el registro de pagos
    BottomSheetPaymentRegister(
        isVisible = isCheckPaymentRegister,
        paymentList = state.paymentList,
        onDismiss = { isCheckPaymentRegister = false }
    )

    // Contenido principal de la pantalla
    Column(
        modifier = Modifier
            .padding(horizontal = 4.dp, vertical = 12.dp)
            .fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Información del pedido
        Text(
            text = "Pedido: ${state.buyOrder.id}",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        OrderSection(
            onClick = { isBuyOrderClicked = !isBuyOrderClicked },
            content = {
                OrderInfoRow(label = "Razón Social", value = state.buyOrder.client)
                OrderInfoRow(label = "Fábrica", value = state.buyOrder.factory)
                if (state.buyOrder.branch.isNotEmpty()) {
                    OrderInfoRow(label = "Segmento", value = state.buyOrder.branch)
                }
                OrderInfoRow(label = "Condición", value = state.buyOrder.paymentCondition)
                OrderInfoRow(label = "Comentarios", value = state.buyOrder.comments)
                OrderInfoRow(label = "Fecha de Carga", value = state.buyOrder.loadedDate)
            }
        )
        AnimatedVisibility(visible = isBuyOrderClicked) {
            BuyOrderItem(buyOrder = state.buyOrder)
        }

        // Divisor horizontal
        HorizontalDivider(
            modifier = Modifier.padding(vertical = 8.dp),
            thickness = 1.dp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
        )

        // Información de facturación
        Text(
            text = "Facturación:",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        OrderSection(
            onClick = { isBillingClicked = !isBillingClicked },
            content = {
                OrderInfoRow(label = "Importe total", value = formatValue(state.totalAmount))
                OrderInfoRow(
                    enable = state.totalDiscount != 0.0,
                    label = "Dto total",
                    value = formatValue(state.totalDiscount)
                )
                OrderInfoRow(
                    enable = state.totalToPay != 0.0,
                    label = "Total a cobrar",
                    value = formatValue(state.totalToPay)
                )
                OrderInfoRow(
                    enable = state.totalPayed != 0.0,
                    label = "Total cobrado",
                    value = formatValue(state.totalPayed)
                )
                OrderInfoRow(
                    enable = state.totalRest != 0.0,
                    label = "Saldo",
                    value = formatValue(state.totalRest)
                )
                if (state.paymentsRegisterNotEmpty) {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        BillingInputChip(
                            onClick = { isCheckPaymentRegister = true },
                            text = "Registro de pagos",
                            imageVector = Icons.AutoMirrored.Filled.List
                        )
                    }
                }
            }
        )

        // Lista de facturas y componentes relacionados
        AnimatedVisibility(visible = isBillingClicked) {
            BillingList(
                billings = if (vm.dataChanged()) tempState.billings else state.billings,
                onBillingClicked = onBillingClicked,
                onAddPaymentCondition = { number ->
                    billingNumber = number
                    isAddPaymentCondition = true
                },
                onAddDeliveryDate = { number ->
                    billingNumber = number
                    isDateSelect = true
                },
                onPaymentRegister = { number ->
                    billingNumber = number
                    isAddPaymentRegister = true
                },
            )

            // BottomSheet para seleccionar condiciones de pago
            BottomSheetPaymentCondition(
                enable = isAddPaymentCondition,
                paymentCondition = state.paymentsConditions,
                factoryName = factoryName,
                onDismissRequest = { isAddPaymentCondition = false },
                onConditionSelected = { paymentCondition ->
                    vm.onSelectedPaymentCondition(paymentCondition, billingNumber)
                    isAddPaymentCondition = false
                }
            )

            // Selector de fecha
            DatePicker(
                enable = isDateSelect,
                onDismissButton = { isDateSelect = false },
                onDismissRequest = { isDateSelect = false },
                onConfirmButton = { newDate ->
                    vm.saveDateSelected(newDate, billingNumber)
                    isDateSelect = false
                }
            )

            // Registro de pagos
            PaymentsRegister(
                enable = isAddPaymentRegister,
                onDismissRequest = { isAddPaymentRegister = false },
                onConfirm = { payed ->
                    isAddPaymentRegister = false
                    scope.launch { vm.addPayment(billingNumber, payed) }
                },
                billingNumber = billingNumber
            )

            if (vm.dataChanged()) {
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = { vm.saveData() },
                    modifier = Modifier.fillMaxWidth().padding(16.dp)
                ) {
                    Text("Guardar Cambios en Facturas")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BottomSheetPaymentRegister(
    isVisible: Boolean,
    paymentList: List<PaymentRegisterModel>,
    onDismiss: () -> Unit
) {
    if (isVisible) {
        val sheetState = rememberModalBottomSheetState()
        val scope = rememberCoroutineScope()

        ModalBottomSheet(
            modifier = Modifier.wrapContentHeight(),
            sheetState = sheetState,
            onDismissRequest = {
                scope.launch {
                    sheetState.hide()
                    onDismiss()
                }
            },
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Registro de pagos",
                    style = MaterialTheme.typography.titleMedium
                )
                PaymentHeaderRow()
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    contentPadding = PaddingValues(8.dp)
                ) {
                    items(paymentList) { payment ->
                        Column {
                            PaymentRow(payment)
                            HorizontalDivider(modifier = Modifier.padding(horizontal = 12.dp))
                        }
                    }
                }
            }
        }
    }
}
@Composable
fun PaymentHeaderRow() {
    val styleText = MaterialTheme.typography.titleSmall
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = "Fecha", modifier = Modifier.weight(1f), style = styleText)
        Text(text = "Documento", modifier = Modifier.weight(1f), style = styleText)
        Text(text = "Tipo", modifier = Modifier.weight(1f), style = styleText)
        Text(text = "Monto", modifier = Modifier.weight(1f), style = styleText)
    }
}

@Composable
fun PaymentRow(payment: PaymentRegisterModel) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = payment.date,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyMedium
        )
        Text(
            text = payment.documentNumber,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyMedium
        )
        Text(
            text = payment.type,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyMedium
        )
        Text(
            text = "$${"%.2f".format(payment.total)}",
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyMedium
        )
    }
}





