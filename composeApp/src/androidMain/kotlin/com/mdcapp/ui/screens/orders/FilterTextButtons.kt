package com.mdcapp.ui.screens.orders

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun FilterTextButtons(
    onFilterPressed: (filter: String, isPressed: Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceAround
    ) {
        var isPendingPressed by remember { mutableStateOf(false) }
        var isInProgressPressed by remember { mutableStateOf(false) }
        var isClosedPressed by remember { mutableStateOf(false) }

        val modifierTextButton: @Composable (Boolean) -> Modifier = { isPressed ->
            Modifier
                .wrapContentSize(Alignment.Center)
                .background(
                    if (isPressed) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.onPrimary,
                    shape = CircleShape
                )
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.primary,
                    shape = CircleShape
                )
                .height(30.dp)
        }
        val styleText = MaterialTheme.typography.bodySmall
        val weightText = FontWeight(500)
        val spacerSize = Modifier.size(8.dp)
        TextButton(
            onClick = {
                isPendingPressed = !isPendingPressed
                isInProgressPressed = false
                isClosedPressed = false
                onFilterPressed("Pending", isPendingPressed)
            },
            modifier = modifierTextButton(isPendingPressed)
        ) {
            Text("Pendiente", style = styleText, fontWeight = weightText)
        }
        Spacer(spacerSize)
        TextButton(
            onClick = {
                isInProgressPressed = !isInProgressPressed
                isClosedPressed = false
                isPendingPressed = false
                onFilterPressed("Progress", isInProgressPressed)
            },
            modifier = modifierTextButton(isInProgressPressed)
        ) {
            Text("En curso", style = styleText, fontWeight = weightText)
        }
        Spacer(spacerSize)
        TextButton(
            onClick = {
                isClosedPressed = !isClosedPressed
                isPendingPressed = false
                isInProgressPressed = false
                onFilterPressed("Closed", isClosedPressed)
            },
            modifier = modifierTextButton(isClosedPressed)
        ) {
            Text("Cerrado", style = styleText, fontWeight = weightText)
        }
    }
}


