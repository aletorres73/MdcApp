package com.mdcapp.ui

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun Screen(content: @Composable (PaddingValues) -> Unit) {
    MaterialTheme {
        Scaffold(modifier = Modifier.fillMaxSize()) { paddingValues ->
            content(paddingValues)
        }
    }
}