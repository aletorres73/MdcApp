package com.mdcapp.ui.composables.billings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.InputChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

@Composable
fun BillingInputChip(
    onClick: () -> Unit = {},
    rowAlignment: Alignment.Vertical = Alignment.CenterVertically,
    rowArrangement: Arrangement.HorizontalOrVertical = Arrangement.spacedBy(4.dp),
    text: String,
    imageVector: ImageVector = Icons.Default.AddCircle
) {
    InputChip(
        onClick = { onClick() },
        label = {
            Row(
                verticalAlignment = rowAlignment,
                horizontalArrangement = rowArrangement
            ) {
                Icon(
                    imageVector = imageVector,
                    contentDescription = null
                )
                Text(text)
            }
        },
        selected = true
    )
}

