package com.mdcapp.ui.screens.orders.orders

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun FilterFactoriesButtons(
    factories: List<String>,
    modifier: Modifier = Modifier,
    onFilterPressed: (filter: String, isPressed: Boolean) -> Unit,
    onReset: (Boolean) -> Unit,
    reset: Boolean = false
) {
    // Estado para almacenar la fábrica seleccionada
    var selectedFactory by remember { mutableStateOf<String?>(null) }

    // Si reset es true, limpiamos la selección
    LaunchedEffect(reset) {
        selectedFactory = null
        onReset(false)
    }

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        factories.forEach { factory ->
            FilterChip(
                selected = factory == selectedFactory, // Solo el chip seleccionado estará activo
                onClick = {
                    // Si el chip ya está seleccionado, lo deseleccionamos
                    if (factory == selectedFactory) {
                        selectedFactory = null
                        onFilterPressed(factory, false)
                    } else {
                        // Seleccionamos el nuevo chip
                        selectedFactory = factory
                        onFilterPressed(factory, true)
                    }
                },
                label = { Text(factory) },
                modifier = Modifier.padding(horizontal = 4.dp)
            )
        }
    }
}
