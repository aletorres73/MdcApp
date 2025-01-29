package com.mdcapp.ui.composables.detailorders

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mdcapp.data.model.BillingModel
import com.mdcapp.data.model.PaymentRegisterModel
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
    orderId: String,
    factoryName: String,
    vm: BuyOrdersViewModel,
    onBillingClicked: (BillingModel) -> Unit
) {
    val state = vm.state
    var isBuyOrderClicked by remember { mutableStateOf(false) }
    var isBillingClicked by remember { mutableStateOf(false) }
    var isDateSelect by remember { mutableStateOf(false) }
    var isAddPaymentCondition by remember { mutableStateOf(false) }
    var isAddPaymentRegister by remember { mutableStateOf(false) }
    var isCheckPaymentRegister by remember { mutableStateOf(false) }
    var billingNumber by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()


    LaunchedEffect(orderId) {
        vm.init(orderId, factoryName)
    }
    LoadingIndicator(
        enabled = state.loadingOrder
                || state.loadingBillings
                || state.loadingPayments
                || state.loadingPaymentsRegister
    )
    BottomSheetPaymentRegister(isCheckPaymentRegister, state.paymentList) {
        isCheckPaymentRegister = false
    }
    Column(
        modifier = Modifier.padding(
            horizontal = 4.dp,
            vertical = 12.dp
        ),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text("Pedido:", style = MaterialTheme.typography.titleMedium)
        OrderSection(
            onClick = { isBuyOrderClicked = !isBuyOrderClicked },
            content = {
                OrderInfoRow(label = "Razón Social", value = state.buyOrder.client)
                OrderInfoRow(label = "Marca", value = state.buyOrder.branch)
                OrderInfoRow(label = "Comentarios", value = state.buyOrder.comments)
                OrderInfoRow(label = "Fecha de Carga", value = state.buyOrder.loadedDate)
                OrderInfoRow(label = "Número de Pedido", value = state.buyOrder.id)
            }
        )
        AnimatedVisibility(isBuyOrderClicked) {
            BuyOrderItem(vm.state.buyOrder)
        }
        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
        Text("Facturación:", style = MaterialTheme.typography.titleMedium)
        OrderSection(
            onClick = { isBillingClicked = !isBillingClicked },
            content = {
                OrderInfoRow(
                    label = "Importe total",
                    value = formatValue(state.totalAmount)
                )
                OrderInfoRow(
                    enable = state.totalDiscount != 0.0,
                    label = "Dto total",
                    value = formatValue(state.totalDiscount)
                )
                OrderInfoRow(
                    enable = state.totalToyPay != 0.0,
                    label = "Total a cobrar",
                    value = formatValue(state.totalToyPay)
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
                            imageVector = Icons.Filled.Payment
                        )
                    }
                }
            }
        )
        AnimatedVisibility(isBillingClicked) {
            BillingList(
                billings = if (vm.dataChanged()) vm.tempState.billings else state.billings,
                onBillingClicked = { billing -> onBillingClicked(billing) },
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
            DatePicker(
                enable = isDateSelect,
                onDismissButton = { isDateSelect = false },
                onDismissRequest = { isDateSelect = false },
                onConfirmButton = { newDate ->
                    println(newDate)
                    vm.saveDateSelected(newDate, billingNumber)
                    isDateSelect = false
                }
            )
            PaymentsRegister(
                enable = isAddPaymentRegister,
                onDismissRequest = { isAddPaymentRegister = false },
                onConfirm = { payed ->
                    println(payed)
                    isAddPaymentCondition = false
                    scope.launch { vm.addPayment(billingNumber, payed) }
                },
                billingNumber = billingNumber
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BottomSheetPaymentRegister(
    enable: Boolean,
    paymentList: List<PaymentRegisterModel>,
    onDismissRequest: () -> Unit
) {
    if (enable) {
        val sheetState = rememberModalBottomSheetState()
        val scope = rememberCoroutineScope()

        ModalBottomSheet(
            modifier = Modifier
                .wrapContentHeight(),
            sheetState = sheetState,
            onDismissRequest = {
                scope.launch {
                    sheetState.hide()
                    onDismissRequest()
                }
            },
        ) {
            Text(
                modifier = Modifier.align(Alignment.CenterHorizontally),
                text = "Registro de pagos",
                style = MaterialTheme.typography.titleMedium
            )
            PaymentHeaderRow()
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(4.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                contentPadding = PaddingValues(8.dp)
            ) {
                items(paymentList, key = null) { payment ->
                    Column {
                        PaymentRow(payment)
                        HorizontalDivider(modifier = Modifier.padding(horizontal = 12.dp))
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
        Text(
            text = "Fecha",
            modifier = Modifier.weight(1f),
            style = styleText
        )
        Text(
            text = "Documento",
            modifier = Modifier.weight(1f),
            style = styleText
        )
        Text(
            text = "Tipo",
            modifier = Modifier.weight(1f),
            style = styleText
        )
        Text(
            text = "Monto",
            modifier = Modifier.weight(1f),
            style = styleText
        )
    }
}

@Composable
fun PaymentRow(payment: PaymentRegisterModel) {
    Row(
        modifier = Modifier
            .fillMaxWidth(),
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





