package com.mdcapp.ui.composables.billings

fun formatValue(value: Double) =
    "$" + String.format("%.2f", value)
