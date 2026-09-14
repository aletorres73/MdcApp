package com.mdcapp.ui

import androidx.compose.runtime.Composable
import com.mdcapp.ui.theme.AppTheme

@Composable
fun Screen(
    content: @Composable () -> Unit
) {
    AppTheme {
        content()
    }
}
