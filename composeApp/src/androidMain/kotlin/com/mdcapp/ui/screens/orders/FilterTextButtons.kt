package com.mdcapp.ui.screens.orders

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.FilterChip
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
fun FilterTextButtons(
    onFilterPressed: (filter: String, isPressed: Boolean) -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceAround
    ) {
        var isPendingPressed by remember { mutableStateOf(false) }
        var isInProgressPressed by remember { mutableStateOf(false) }
        var isClosedPressed by remember { mutableStateOf(false) }

        val spacerSize = Modifier.size(8.dp)

        FilterChip(
            selected = isPendingPressed,
            onClick = {
                isPendingPressed = !isPendingPressed
                isInProgressPressed = false
                isClosedPressed = false
                onFilterPressed("Pending", isPendingPressed)
            },
            label = { Text("Pendiente") },
            modifier = Modifier.padding(horizontal = 4.dp)
        )

        Spacer(spacerSize)

        FilterChip(
            selected = isInProgressPressed,
            onClick = {
                isInProgressPressed = !isInProgressPressed
                isClosedPressed = false
                isPendingPressed = false
                onFilterPressed("Progress", isInProgressPressed)
            },
            label = { Text("En curso") },
            modifier = Modifier.padding(horizontal = 4.dp)
        )

        Spacer(spacerSize)

        FilterChip(
            selected = isClosedPressed,
            onClick = {
                isClosedPressed = !isClosedPressed
                isPendingPressed = false
                isInProgressPressed = false
                onFilterPressed("Closed", isClosedPressed)
            },
            label = { Text("Cerrado") },
            modifier = Modifier.padding(horizontal = 4.dp)
        )
    }
}


