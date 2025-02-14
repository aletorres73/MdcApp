package com.mdcapp.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.mdcapp.ui.navigation.AndroidNavigation
import com.mdcapp.ui.navigation.BottomBarNavigation

@Composable
actual fun PlatformNavigation() {
    Screen {
        var route by remember { mutableStateOf("Home") }

        Scaffold(
            bottomBar = {
                BottomBarNavigation { newRoute -> route = newRoute }
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                AndroidNavigation(
                    startRoute = route
                )
            }
        }
    }
}