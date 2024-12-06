package com.mdcapp.domain.usescases

import com.mdcapp.domain.remote.OrderRepository

class HandlersUsesCases(repository: OrderRepository) {
    companion object {
        const val ORDER = "orderDetail"
        const val BUY_ORDER = "buyOrderDetails"
        const val BILLING = "billingsDetail"
    }

    fun loadValues(key: String, value: String): Boolean = when (key) {
        ORDER -> {
            handleOrderInfoClick(value)
            true
        }

        BUY_ORDER -> {
            handleBuyOrderDetailClick(value)
            true
        }

        BILLING -> {
            handleBillingInfoClick(value)
            true
        }

        else -> {
            false
        }
    }


    private fun handleOrderInfoClick(orderNumber: String) {
        println("Información de la Orden: $orderNumber")
    }

    private fun handleBuyOrderDetailClick(orderNumber: String) {
        println("Información del Pedido: $orderNumber")
    }

    private fun handleBillingInfoClick(orderNumber: String) {
        println("Información de Facturación y Pagos: $orderNumber")
    }
}