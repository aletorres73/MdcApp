package com.mdcapp.ui.screens.invoicesClientDetail

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun HeaderCell(
    text: String,
    style: TextStyle,
    width: Dp,
    textAlign: TextAlign = TextAlign.Start
) {
    Text(
        text = text,
        style = style,
        textAlign = textAlign,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        modifier = Modifier
            .width(width)
            .padding(horizontal = 4.dp)
    )
}

@Composable
fun DataCell(
    text: String,
    style: TextStyle,
    width: Dp,
    textAlign: TextAlign = TextAlign.Start
) {
    Text(
        text = text,
        style = style,
        textAlign = textAlign,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        modifier = Modifier
            .width(width)
            .padding(horizontal = 4.dp)
    )
}
