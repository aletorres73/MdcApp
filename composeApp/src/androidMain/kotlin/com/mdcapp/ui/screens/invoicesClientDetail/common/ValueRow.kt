package com.mdcapp.ui.screens.invoicesClientDetail.common

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun ValueRow(
    label: String,
    value: String,
    highlight: Boolean = false
) {
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {

        Text(
            label,
            style = MaterialTheme.typography.bodyMedium
        )

        Text(
            value,
            style = if (highlight)
                MaterialTheme.typography.titleMedium
            else
                MaterialTheme.typography.bodyMedium
        )
    }
}