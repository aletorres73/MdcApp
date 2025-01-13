package com.mdcapp.ui.screens.home

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mdcapp.ui.Screen
import com.mdcapp.ui.composables.common.LoadingIndicator
import com.mdcapp.ui.viewmodels.HomeViewModel
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.annotation.KoinExperimentalAPI

@OptIn(KoinExperimentalAPI::class)
@Composable
fun HomeScreen(
    vm: HomeViewModel = koinViewModel(),
    onFactory: (String) -> Unit
) {
    val state = vm.state
    Screen {
        Scaffold { paddingValues ->
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(5.dp),
                    contentPadding = paddingValues,
                    horizontalAlignment = Alignment.Start,
                ) {
                    items(state.factoryList, key = null) {
                        Spacer(modifier = Modifier.size(4.dp))
                        FactoryItem(it) {
                            onFactory(it.name)
                        }
                    }
                }
            }
            LoadingIndicator(state.loading)
        }
    }
}

