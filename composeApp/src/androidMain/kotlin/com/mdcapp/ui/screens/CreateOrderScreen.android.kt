package com.mdcapp.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.mdcapp.ui.viewmodels.orders.CreateOrderViewModel

@Composable
actual fun AddArticleOverlay(
    state: CreateOrderViewModel.UiState,
    onNameChange: (TextFieldValue) -> Unit,
    onColorChange: (TextFieldValue) -> Unit,
    onPairsChange: (String) -> Unit,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit,
    onConfirm: () -> Unit,
    onConfirmAndContinue: () -> Unit,
    onDismiss: () -> Unit,
    onClearName: () -> Unit,
    onClearColor: () -> Unit,
    isVisible: Boolean
) {
    if (isVisible) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text("Agregar Artículo") },
            text = {
                AddArticleContent(
                    state = state,
                    onNameChange = onNameChange,
                    onColorChange = onColorChange,
                    onPairsChange = onPairsChange,
                    onIncrement = onIncrement,
                    onDecrement = onDecrement,
                    onClearName = onClearName,
                    onClearColor = onClearColor
                )
            },
            confirmButton = {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextButton(onClick = onConfirmAndContinue) {
                        Text("Continuar")
                    }
                    Button(onClick = onConfirm) {
                        Text("Cerrar")
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) { Text("Cancelar") }
            }
        )
    }
}
