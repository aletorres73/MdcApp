package com.mdcapp.ui.screens.invoicesClientDetail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mdcapp.data.model.PaymentCondition
import kotlin.math.roundToInt

@Composable
fun PaymentConditionListSheet(
    list: List<PaymentCondition>,
    onSelect: (PaymentCondition) -> Unit
) {
    val editedList by remember { mutableStateOf(list) }

    Column(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp, horizontal = 8.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text("Condiciones de Pago", style = MaterialTheme.typography.titleMedium)

        Spacer(Modifier.height(12.dp))

        editedList.forEachIndexed { _, item ->
            var name by remember { mutableStateOf(item.paymentName) }
            var discount by remember {
                mutableStateOf(
                    (item.discount * 100).roundToInt().toString()
                )
            }
            var month by remember { mutableStateOf(item.month.toString()) }
            var expiration by remember { mutableStateOf(item.expiration.toString()) }

            Card(
                Modifier
                    .fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    val textStyle = MaterialTheme.typography.labelMedium
                    TextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Nombre") })
                    TextField(
                        value = discount,
                        onValueChange = { discount = it },
                        label = { Text("Descuento") },
                        prefix = { Text("%") })
                    TextField(
                        value = month,
                        onValueChange = { month = it },
                        label = { Text("Plazo") })
                    TextField(
                        value = expiration,
                        onValueChange = { expiration = it },
                        label = { Text("Vencimiento (días)") })

                    Button(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = {
                            // Crea objeto modificado y lo envía
                            val updated = item.copy(
                                paymentName = name,
                                discount = discount.toDoubleOrNull() ?: 0.0,
                                month = month.toIntOrNull() ?: 0,
                                expiration = expiration.toIntOrNull() ?: 0,
                            )
                            onSelect(updated)
                        }
                    ) {
                        Text("Seleccionar")
                    }
                }
            }
        }
    }
}
