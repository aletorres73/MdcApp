package com.mdcapp.ui.composables.common

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun SearchbarTopBar(
    query: TextFieldValue,
    onQueryChange: (TextFieldValue) -> Unit,
    onCleanQuery: () -> Unit,
    onClose: () -> Unit,
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
        keyboardController?.show()
    }

    TextField(
        value = query,
        textStyle = TextStyle(fontSize = 18.sp),
        onValueChange = onQueryChange,
        modifier = Modifier
            .wrapContentHeight()
            .fillMaxWidth()
            .focusRequester(focusRequester),
        placeholder = { Text(text = "Buscar por artículo...", fontSize = 18.sp) },
        singleLine = true,
        shape = RoundedCornerShape(12.dp),
        trailingIcon = {
            IconButton(onClick = onCleanQuery) {
                Icon(imageVector = Icons.Default.Clear, contentDescription = "Clear query")
            }
        },
        leadingIcon = {
            IconButton(onClick = onClose) {
                Icon(
                    imageVector = Icons.Filled.ArrowBackIosNew,
                    contentDescription = "Back to TopBar"
                )
            }
        },
    )

}
