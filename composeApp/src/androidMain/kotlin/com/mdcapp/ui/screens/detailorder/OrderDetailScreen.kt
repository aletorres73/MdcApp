package com.mdcapp.ui.screens.detailorder

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import com.mdcapp.ui.Screen
import com.mdcapp.ui.composables.common.MainTopAppBar
import com.mdcapp.ui.composables.detailorders.OrderDetailInfo

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderDetailScreen(
    orderId: String,
    factoryName: String,
    onBackPressed: () -> Unit,
) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    Screen {
        Scaffold(
            topBar = {
                MainTopAppBar(
                    scrollBehavior = scrollBehavior,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.onPrimaryContainer),
                    titleContent = {
                        Text(
                            "Orden \t$orderId",
                            style = MaterialTheme.typography.titleMedium
                        )
                    },
                    isNavigationOn = true,
                    onBack = onBackPressed
                )
            },
            modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                horizontalAlignment = Alignment.Start
            ) {
                HorizontalDivider()
                OrderDetailInfo(
                    orderId = orderId,
                    onBillingClicked = {},
                    factoryName = factoryName
                )
            }
        }
    }
}