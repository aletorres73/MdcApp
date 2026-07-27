package com.mdcapp.ui.screens.invoicesPage

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.InputChip
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StateFilter(
    states: List<String>,
    selected: String,
    counts: Map<String, Int> = emptyMap(),
    onSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Box(
        contentAlignment = Alignment.CenterEnd
    ) {
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            InputChip(
                selected = false,
                onClick = { expanded = true },
                label = { Text("$selected (${counts[selected] ?: 0})") },
                modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryEditable, true)
            )

            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.widthIn(min = 160.dp)
            ) {
                states.forEach { state ->
                    val count = counts[state] ?: 0
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = "$state ($count)",
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        },
                        onClick = {
                            expanded = false
                            onSelected(state)
                        }
                    )
                }
            }
        }
    }
}

