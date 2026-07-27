package com.mdcapp.ui.composables.billings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.mdcapp.domain.entities.PaymentCondition

@Composable
fun inputPaymentCondition(): PaymentCondition {
    var paymentName by remember { mutableStateOf(TextFieldValue("")) }
    var discount by remember { mutableStateOf(TextFieldValue("")) }
    var month by remember { mutableStateOf(TextFieldValue("")) }
    var expiration by remember { mutableStateOf(TextFieldValue("")) }
    var date by remember { mutableStateOf(TextFieldValue("")) }

    val paymentInputList = listOf(
        "condicion" to paymentName,
        "descuento" to discount,
        "meses" to month,
        "vencimiento" to expiration,
        "plazo" to date
    )

    val modifierTextField = Modifier
        .fillMaxWidth()
        .wrapContentHeight()
    val textStyle = MaterialTheme.typography.bodySmall

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        paymentInputList.forEach { (key, value) ->
            OutlinedTextField(
                modifier = modifierTextField,
                textStyle = textStyle,
                value = value,
                onValueChange = { newValue ->
                    when (key) {
                        "condicion" -> paymentName = newValue
                        "descuento" -> discount = newValue
                        "meses" -> month = newValue
                        "vencimiento" -> expiration = newValue
                        "plazo" -> date = newValue
                    }
                },
                label = { Text(key) },
                shape = RoundedCornerShape(12.dp),
            )
        }
    }

    return PaymentCondition(
        paymentName = paymentName.text,
        discount = discount.text.toDoubleOrNull() ?: 0.0,
        month = month.text.toIntOrNull() ?: 0,
        expiration = expiration.text.toIntOrNull() ?: 0,
        date = date.text.toIntOrNull() ?: 0
    )
}

