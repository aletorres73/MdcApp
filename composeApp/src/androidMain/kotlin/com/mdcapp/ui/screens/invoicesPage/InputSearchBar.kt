package com.mdcapp.ui.screens.invoicesPage

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.InputChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.dp
import com.mdcapp.domain.entities.TypeSearch

@Composable
fun InputSearchBar(onTypeSelected: (TypeSearch) -> Unit) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        var selectedInput by remember { mutableStateOf(false) }
        InputChip(
            selected = !selectedInput,
            onClick = {
                onTypeSelected(TypeSearch.Client)
                selectedInput = !selectedInput
            },
            label = { Text("Cliente") }
        )
        InputChip(
            selected = selectedInput,
            onClick = {
                onTypeSelected(TypeSearch.Number)
                selectedInput = !selectedInput
            },
            label = { Text("Numero") }
        )
    }
}

