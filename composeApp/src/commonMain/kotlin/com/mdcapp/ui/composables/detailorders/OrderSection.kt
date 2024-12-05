package com.mdcapp.ui.composables.detailorders

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun OrderSection(
    title: String,
    content: @Composable () -> Unit,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.padding(vertical = 4.dp).clickable { onClick() },
        elevation = CardDefaults.elevatedCardElevation()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            content()
        }
    }
}
