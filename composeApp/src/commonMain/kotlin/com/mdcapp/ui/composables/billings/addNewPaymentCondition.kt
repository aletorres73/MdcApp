package com.mdcapp.ui.composables.billings

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.InputChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun AddNewPaymentCondition() {
    var isAddedButton by remember { mutableStateOf(false) }
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AnimatedVisibility(isAddedButton) {
            Column {
                inputPaymentCondition()
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    val rowAlignment = Alignment.CenterVertically
                    val rowArrangement = Arrangement.spacedBy(4.dp)
                    InputChip(
                        onClick = { isAddedButton = false },
                        label = {
                            Row(
                                verticalAlignment = rowAlignment,
                                horizontalArrangement = rowArrangement
                            ) {
                                Text("Cancelar")
                            }
                        },
                        selected = true
                    )
                    InputChip(
                        onClick = { isAddedButton = false },
                        label = {
                            Row(
                                verticalAlignment = rowAlignment,
                                horizontalArrangement = rowArrangement
                            ) {
                                Text("Agregar")
                            }
                        },
                        selected = true
                    )
                }
            }
        }
        Button(
            modifier = Modifier
                .padding(16.dp),
            onClick = { isAddedButton = !isAddedButton }
        ) {
            Text("Nueva condición")
        }
    }
}

