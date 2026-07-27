package com.mdcapp.ui.composables.billings

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun RowInfoBilling(
    modifier: Modifier = Modifier,
    key: String = "",
    value: String,
    isTitle: Boolean = false // Nuevo parámetro para aplicar estilo de título
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp), // Espaciado vertical para mejor separación
        verticalAlignment = Alignment.CenterVertically,
//        horizontalArrangement = Arrangement.spacedBy(18.dp) // Alinear texto y valor
    ) {
        Text(
            modifier = Modifier.weight(1f),
            text = key,
            style = if (isTitle) MaterialTheme.typography.titleMedium else MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = if (isTitle) 1f else 0.6f) // Color más suave para texto no titular
        )
        Text(
            modifier = Modifier.weight(1f),
            text = value,
            style = if (isTitle) MaterialTheme.typography.titleMedium else MaterialTheme.typography.bodyMedium,
            fontWeight = if (isTitle) FontWeight.Bold else FontWeight.Normal, // Negrita para títulos
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
