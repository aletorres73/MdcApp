package com.mdcapp.ui.composables.common

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainTopAppBar(
    title: String = String(),
    scrollBehavior: TopAppBarScrollBehavior,
    onBack: () -> Unit = {},
    isNavigationOn: Boolean = false,
    onSearchIconClick: () -> Unit = {},
    onActions: @Composable () -> Unit
) {
    TopAppBar(
        title = {
            Text(
                text = title,
                fontSize = 18.sp
            )
        },
        scrollBehavior = scrollBehavior,
        actions = { onActions() },
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