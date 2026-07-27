package com.mdcapp.ui.composables.common

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

@Composable
fun Dialog(
    enable: Boolean,
    titleText: String,
    text: String,
    onConfirmButton: () -> Unit,
    onDismissButton: () -> Unit,
    onDismissRequest: () -> Unit,
    confirmTextButton: String,
    dismissTextButton: String,
    iconTitle: ImageVector,
    content: @Composable () -> Unit = {}
) {
    if (enable) {
        AlertDialog(
            onDismissRequest = { onDismissRequest() },
            icon = {
                Icon(
                    iconTitle,
                    contentDescription = null,
                )
            },
            title = {
                if (titleText.isNotEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) { Text(text = titleText) }
                }
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(2.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = text)
                    content()
                }
            },
            confirmButton = {
                TextButton(
                    onClick = { onConfirmButton() },
                    modifier = Modifier.padding(horizontal = 25.dp)
                ) {
                    Text(confirmTextButton)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { onDismissButton() },
                    modifier = Modifier.padding(horizontal = 25.dp)
                ) {
                    Text(dismissTextButton)
                }
            }
        )
    }
}
