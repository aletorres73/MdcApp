package com.mdcapp.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable

@Composable
fun Screen(
    content: @Composable () -> Unit
) {
    MaterialTheme {
        content()
    }
}
