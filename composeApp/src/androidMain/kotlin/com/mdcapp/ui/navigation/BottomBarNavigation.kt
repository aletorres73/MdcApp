package com.mdcapp.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun BottomBarNavigation(onNavigationIcon: (String) -> Unit) {
    var isHomePressed by remember { mutableStateOf(true) }
    var isClientPressed by remember { mutableStateOf(false) }
//    var isHomePressed by remember { mutableStateOf(false) }
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

    BottomAppBar(modifier = Modifier.height(50.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            TextButton(
                modifier = modifierTextButton(isHomePressed),
                onClick = {
                    isHomePressed = true
                    isClientPressed = false
                    onNavigationIcon("Home")
                }
            ) { Icon(imageVector = Icons.Filled.Home, contentDescription = null) }
            TextButton(
                modifier = modifierTextButton(isClientPressed),
                onClick = {
                    isClientPressed = true
                    isHomePressed = false
                    onNavigationIcon("Clients")
                }
            ) { Icon(imageVector = Icons.Filled.Person, contentDescription = null) }
            /*        TextButton(
                        modifier = modifierTextButton(isNavigationPressed),
                        onClick = { isNavigationPressed = !isNavigationPressed }
                    ) { Icon(imageVector = Icons.Default.Home, contentDescription = null) }*/
        }
    }

}