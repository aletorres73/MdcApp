package com.mdcapp.ui.composables.common

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
expect fun RefreshContainer(
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
)
