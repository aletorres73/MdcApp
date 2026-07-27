package com.mdcapp.ui.screens.invoicesClientDetail

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import com.mdcapp.domain.entities.BillingModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InvoiceHeaderTopBar(
    billing: BillingModel,
    onBack: () -> Unit
) {
    TopAppBar(
        scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(),
        title = { InvoiceHeaderCard(billing = billing) },
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
            }
        }
    )
}

