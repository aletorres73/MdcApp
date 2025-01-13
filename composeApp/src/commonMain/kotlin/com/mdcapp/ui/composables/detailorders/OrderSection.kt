package com.mdcapp.ui.composables.detailorders

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
    title: String = "",
    content: @Composable () -> Unit,
    onClick: () -> Unit
) {
    Card(
        elevation = CardDefaults.elevatedCardElevation(6.dp)
    ) {
        Column(
            modifier = Modifier
                .clickable { onClick() }
                .padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            if (title.isNotEmpty())
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            content()
        }
    }
}
