package com.mdcapp.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.rememberWindowState
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
        Window(
            onCloseRequest = onDismiss,
            title = "Agregar Artículo",
            state = rememberWindowState(size = DpSize(500.dp, 600.dp))
        ) {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp)
                    ) {
                        Box(modifier = Modifier.weight(1f)) {
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
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            TextButton(onClick = onDismiss) {
                                Text("Cancelar")
                            }
                            TextButton(onClick = onConfirmAndContinue) {
                                Text("Continuar")
                            }
                            Button(onClick = onConfirm) {
                                Text("Cerrar")
                            }
                        }
                    }
                }
            }
        }
    }
}
