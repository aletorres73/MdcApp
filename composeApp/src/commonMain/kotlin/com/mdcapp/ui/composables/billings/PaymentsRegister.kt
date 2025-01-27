package com.mdcapp.ui.composables.billings

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

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
        val keyboardController = LocalSoftwareKeyboardController.current

        ModalBottomSheet(
            modifier = Modifier
                .wrapContentHeight(),
            sheetState = sheetState,
            onDismissRequest = {
                scope.launch {
                    keyboardController?.hide()
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
                TextField(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight(),
                    value = inputPay,
                    onValueChange = { newValue -> inputPay = newValue },
                    label = { Text("Ingresar monto") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    prefix = { Text("$") },
                    singleLine = true
                )
                Button(onClick = {
                    println(inputPay)
                    scope.launch {
//                        keyboardController?.hide()
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
