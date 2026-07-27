package com.mdcapp.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * Centraliza los colores semánticos para los estados de facturación.
 * Asegura que el "semáforo" sea consistente en todas las pantallas.
 */
fun getBillingStatusColor(state: String): Color = when (state) {
    "Cobrado", "Cerrada" -> Color(0xFF2E7D32) // Verde: Éxito / Finalizado
    "Vencido" -> Color(0xFFC62828)           // Rojo: Alerta crítica
    "Por vencer" -> Color(0xFFF9A825)        // Ámbar: Precaución / Próximo
    "Pendiente", "En proceso" -> Color(0xFF1976D2) // Azul: Informativo / Activo
    "Devuelta", "Cancelado" -> Color(0xFF616161)   // Gris: Neutro / Inactivo
    else -> Color.Gray
}
