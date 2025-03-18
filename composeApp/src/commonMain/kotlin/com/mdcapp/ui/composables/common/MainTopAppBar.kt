package com.mdcapp.ui.composables.common

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainTopAppBar(
    title: String = String(),
    scrollBehavior: TopAppBarScrollBehavior,
    onBack: () -> Unit = {},
    isNavigationOn: Boolean = false,
    onActions: @Composable () -> Unit = {},
    modifier: Modifier = Modifier
) {
    TopAppBar(
        modifier = modifier,
        title = {
            Text(
                text = title,
                fontSize = 18.sp
            )
        },
        scrollBehavior = scrollBehavior,
        actions = { onActions() },
        navigationIcon = {
            NavigationButton(isNavigationOn, onBack)
        }
    )
}