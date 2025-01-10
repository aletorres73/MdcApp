package com.mdcapp.ui.home

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.mdcapp.ui.Screen
import com.mdcapp.ui.composables.common.LoadingIndicator
import com.mdcapp.ui.viewmodels.HomeViewModel
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.annotation.KoinExperimentalAPI

@OptIn(KoinExperimentalAPI::class)
@Composable
fun HomeScreen(
    vm: HomeViewModel = koinViewModel()
) {
    val state = vm.state
    Screen {
        Scaffold { paddingValues ->
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = paddingValues,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                items(state.factoryList, key = null) {
                    Column {
                        Text(text = it.name)
                        if (it.branchList.isNotEmpty())
                            Row {
                                it.branchList.forEach { branch ->
                                    Row { Text(branch) }
                                }
                            }
                    }
                }

            }
            LoadingIndicator(state.loading)
        }
    }
}