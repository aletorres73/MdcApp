package com.mdcapp.ui.navigation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.sharp.Home
import androidx.compose.material.icons.sharp.Person
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mdcapp.domain.entities.AppRoute

@Composable
fun BottomBarNavigation(onNavigationIcon: (AppRoute) -> Unit) {
    BottomAppBar(modifier = Modifier.height(50.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            TextButton(
                onClick = { onNavigationIcon(AppRoute.InvoicesPaged) }
            ) {
                Icon(imageVector = Icons.Sharp.Home, contentDescription = null)
            }
            TextButton(
                onClick = { onNavigationIcon(AppRoute.Clients) }
            ) { Icon(imageVector = Icons.Sharp.Person, contentDescription = null) }
        }
    }

}
