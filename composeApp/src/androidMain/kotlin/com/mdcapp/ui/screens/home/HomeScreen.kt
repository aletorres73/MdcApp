package com.mdcapp.ui.screens.home

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.mdcapp.data.model.FactoryModel
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
                            onFactory(
                                if (it.name == "IBA") "Gummi" else it.name
                            )
                        }
                    }
                }
            }
            LoadingIndicator(state.loading)
        }

    }
}

@Composable
fun FactoryItem(
    factory: FactoryModel,
    onFactory: () -> Unit
) {
    Surface(
        modifier = Modifier
            .padding(4.dp),
        shape = RoundedCornerShape(10.dp),
        shadowElevation = 6.dp,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .border(0.85.dp, Color.LightGray, RoundedCornerShape(10.dp))
                .padding(4.dp)
                .clickable { onFactory() }
        ) {
            Text(text = factory.name, modifier = Modifier.align(Alignment.CenterHorizontally))
        }
    }
}