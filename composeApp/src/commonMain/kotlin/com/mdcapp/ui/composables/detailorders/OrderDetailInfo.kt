package com.mdcapp.ui.composables.detailorders

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.mdcapp.data.model.BillingModel
import com.mdcapp.ui.composables.billings.BillingList
import com.mdcapp.ui.composables.billings.BottomSheetPaymentCondition
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
    var billingNumber by remember { mutableStateOf("") }


    LaunchedEffect(orderId) {
        vm.init(orderId, factoryName)
    }

    Box {
        LoadingIndicator(
            enabled = state.loadingOrder && state.loadingBillings && state.loadingPayments
        )
        Column(
            modifier = Modifier.padding(
                horizontal = 8.dp,
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
                    }
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
                        vm.addPayment(billingNumber, payed)
                    },
                    billingNumber = billingNumber
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentsRegister(
    enable: Boolean,
    onDismissRequest: () -> Unit,
    onConfirm: (Double) -> Unit,
    billingNumber: String
) {
    if (enable) {
        var inputPay by remember { mutableStateOf(TextFieldValue("")) }
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
            }
        ) {
            Column(
                modifier = Modifier
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "Registrar pago en $billingNumber",
                    style = MaterialTheme.typography.titleMedium,
                )
                HorizontalDivider()
                OutlinedTextField(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight(),
                    value = inputPay,
                    onValueChange = { newValue -> inputPay = newValue },
                    label = { Text("Ingresar monto") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    prefix = { Text("$") }
                )
                Button(onClick = {
                    println(inputPay)
                    scope.launch {
                        sheetState.hide()
                        onConfirm(inputPay.text.toDouble())
                    }
                }) {
                    Text("Agregar pago")
                }
            }
        }
    }
}




