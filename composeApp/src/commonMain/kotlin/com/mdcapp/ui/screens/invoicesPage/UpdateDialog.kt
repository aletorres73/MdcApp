package com.mdcapp.ui.screens.invoicesPage

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mdcapp.domain.entities.UpdateState

@Composable
fun UpdateDialog(
    type: UpdateState,
    releaseNotes: String,
    isDownloading: Boolean = false,
    onUpdate: () -> Unit,
    onDismiss: () -> Unit
) {

    val force = type == UpdateState.FORCE_UPDATE

    AlertDialog(
        onDismissRequest = {
            if (!force && !isDownloading) onDismiss()
        },
        title = {
            Text(
                if (force) "Actualización requerida"
                else "Nueva actualización disponible"
            )
        },
        text = {
            Column {
                Text(
                    if (force)
                        "Debes actualizar la aplicación para continuar."
                    else
                        "Hay una nueva versión disponible."
                )

                if (releaseNotes.isNotBlank()) {
                    Spacer(Modifier.height(12.dp))
                    Text(releaseNotes)
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = onUpdate,
                enabled = !isDownloading
            ) {
                Text(if (isDownloading) "Descargando..." else "Actualizar")
            }
        },
        dismissButton = {
            if (!force) {
                TextButton(
                    onClick = onDismiss,
                    enabled = !isDownloading
                ) {
                    Text("Más tarde")
                }
            }
        }
    )
}
