package com.mdcapp.ui.composables.common

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable

@Composable
fun SearchButton(isNavigationOn: Boolean, onSearchIconClick: () -> Unit) {
    if (!isNavigationOn) {
        IconButton(onClick = onSearchIconClick) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = null,
            )
        }
    }
}
