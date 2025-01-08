package com.mdcapp.ui.composables.common

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainTopAppBar(
    titleContent: @Composable () -> Unit = {},
    scrollBehavior: TopAppBarScrollBehavior,
    onBack: () -> Unit = {},
    isNavigationOn: Boolean = false,
    onSearchIconClick: () -> Unit = {},
    onActions: @Composable () -> Unit = {},
    modifier: Modifier = Modifier
) {
    TopAppBar(
        modifier = modifier,
        title = { titleContent() },
        scrollBehavior = scrollBehavior,
//        actions = { onActions() },
        navigationIcon = {
            if (isNavigationOn) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        null,
                    )
                }
            }
        }
    )
}