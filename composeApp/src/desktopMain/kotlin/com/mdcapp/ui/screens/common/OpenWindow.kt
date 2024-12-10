package com.mdcapp.ui.screens.common

import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.rememberWindowState

@Composable
fun OpenWindow(
    onCloseRequest: () -> Unit = {},
    title: String,
    content: @Composable () -> Unit = {},
    size: DpSize = DpSize(600.dp, 780.dp),
    position: WindowPosition = WindowPosition(alignment = Alignment.CenterEnd),
    onTop: Boolean = false
) {
    val windowState = rememberWindowState(
        size = size,
        position = position
    )
    Window(
        onCloseRequest = { onCloseRequest() },
        title = title,
        state = windowState,
        resizable = false,
        alwaysOnTop = onTop
    ) {
        content()
    }
}