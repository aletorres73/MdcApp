package com.mdcapp.domain.entities

sealed class TypeSearch {
    data object Client : TypeSearch()
    data object Number : TypeSearch()
}
