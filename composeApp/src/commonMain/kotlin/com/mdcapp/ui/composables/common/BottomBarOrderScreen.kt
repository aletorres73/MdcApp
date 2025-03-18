package com.mdcapp.ui.composables.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

@Composable
fun BottomBarOrderScreen(onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = MaterialTheme.colorScheme.surface,
            )
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceAround
    ) {
        IconColumn(onClick, Icons.Default.Home, "Inicio")
        IconColumn(onClick, Icons.Default.AddCircle, "Nuevo Pedido")
        IconColumn(onClick, Icons.Default.Person, "Clientes")
    }
}

@Composable
private fun IconColumn(
    onClick: () -> Unit,
    icon: ImageVector,
    description: String
) {
    val modifier = Modifier
        .shadow(
            elevation = 5.dp,
            shape = CircleShape,
        )
        .clip(CircleShape)
    val buttonColor = IconButtonDefaults.iconButtonColors(MaterialTheme.colorScheme.primary)
    val iconColor = MaterialTheme.colorScheme.primaryContainer

    Column(
        verticalArrangement = Arrangement.spacedBy(2.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        IconButton(
            onClick = { onClick() },
            modifier = modifier,
            colors = buttonColor
        ) { Icon(imageVector = icon, contentDescription = null, tint = iconColor) }
        Text(description, style = MaterialTheme.typography.labelSmall)
    }
}