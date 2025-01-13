package com.mdcapp.ui.screens.home

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.mdcapp.data.model.FactoryModel

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
                .clickable { onFactory() }
                .padding(4.dp)
        ) {
            Text(text = factory.name, modifier = Modifier.align(Alignment.CenterHorizontally))
        }
    }
}