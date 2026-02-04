package com.mdcapp.ui.composables.common

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun SearchBar(
    query: TextFieldValue,
    onQueryChange: (TextFieldValue) -> Unit,
    onCleanQuery: () -> Unit,
    onSearch: (TextFieldValue) -> Unit = {},
    searchText: String = "Buscar cliente..."
) {

    OutlinedTextField(
        value = query,
        textStyle = TextStyle(fontSize = 18.sp),
        onValueChange = onQueryChange,
        modifier = Modifier
            .wrapContentHeight()
            .fillMaxWidth()
            .padding(horizontal = 4.dp),
        placeholder = { Text(text = searchText, fontSize = 18.sp) },
        singleLine = true,
        shape = RoundedCornerShape(12.dp),

        trailingIcon = {
            if (query.text.isNotEmpty()) {
                IconButton(onClick = onCleanQuery) {
                    Icon(
                        imageVector = Icons.Default.Clear,
                        contentDescription = "Clear query"
                    )
                }
            }
        },
        keyboardOptions = KeyboardOptions(
            capitalization = KeyboardCapitalization.Words,
            imeAction = ImeAction.Search
        ),
        keyboardActions = KeyboardActions(
            onSearch = {
                onSearch(query)
            }
        )
    )
}
