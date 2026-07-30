package com.mdcapp.domain.logic

import com.mdcapp.domain.entities.BillingModel
import com.mdcapp.domain.entities.BuyOrderModel
import com.mdcapp.domain.entities.toFormattedDate
import com.mdcapp.domain.entities.toPrint

object ReportGenerator {

    fun generateOrderReport(order: BuyOrderModel): String {
        val sb = StringBuilder()
        sb.append("📝 *NOTA DE PEDIDO*\n")
        sb.append("----------------------------\n")
        sb.append("🆔 *Orden:* #${order.order}\n")
        sb.append("👤 *Cliente:* ${order.client}\n")
        sb.append("🏭 *Fábrica:* ${order.factory} (${order.branch})\n")
        sb.append("📅 *Fecha de Entrega:* ${order.deliveryDate.toFormattedDate()}\n")
        sb.append("\n📦 *Detalle del Pedido:*\n")

        order.articles.forEach { article ->
            sb.append("• ${article.name} (${article.color}): ${article.pairs} pares\n")
        }

        if (order.comments.isNotEmpty()) {
            sb.append("\n💬 *Comentarios:* ${order.comments}\n")
        }

        sb.append("----------------------------\n")
        sb.append("Generado por MDCapp")
        return sb.toString()
    }

    fun generateInvoiceReport(billing: BillingModel): String {
        val sb = StringBuilder()
        sb.append("📄 *INFORMACIÓN DE FACTURA*\n")
        sb.append("----------------------------\n")
        sb.append("🔢 *Número:* ${billing.billingNumber}\n")
        sb.append("👤 *Cliente:* ${billing.clientName}\n")
        sb.append("🏷️ *Marca:* ${billing.brand}\n")
        sb.append("💰 *Monto Total:* ${billing.total.toPrint()}\n")
        sb.append("📅 *Vencimiento:* ${billing.payDate.toFormattedDate()}\n")
        sb.append("📌 *Estado:* ${billing.stateBilling}\n")

        if (billing.rest > 0) {
            sb.append("⚠️ *Saldo Pendiente:* ${billing.rest.toPrint()}\n")
        } else {
            sb.append("✅ *Estado:* Totalmente Cobrada\n")
        }

        sb.append("----------------------------\n")
        sb.append("Generado por MDCapp")
        return sb.toString()
    }

    fun generateCurrentAccountReport(clientName: String, billings: List<BillingModel>): String {
        val pendingBillings = billings.filter { it.rest > 0 }
        val totalDebt = pendingBillings.sumOf { it.rest }
        val expiredDebt = pendingBillings.filter { it.stateBilling == "Vencido" }.sumOf { it.rest }

        val sb = StringBuilder()
        sb.append("📊 *ESTADO DE CUENTA*\n")
        sb.append("----------------------------\n")
        sb.append("👤 *Cliente:* $clientName\n")
        sb.append("📅 *Fecha:* ${System.currentTimeMillis().toFormattedDate()}\n\n")

        sb.append("💰 *Saldo Total:* ${totalDebt.toPrint()}\n")
        if (expiredDebt > 0) {
            sb.append("🔴 *Saldo Vencido:* ${expiredDebt.toPrint()}\n")
        }

        sb.append("\n📑 *Documentos Pendientes:*\n")
        if (pendingBillings.isEmpty()) {
            sb.append("✅ No se registran facturas pendientes.\n")
        } else {
            pendingBillings.forEach { billing ->
                val emoji = if (billing.stateBilling == "Vencido") "🔴" else "🟡"
                sb.append("$emoji *Fact:* ${billing.billingNumber} - *Vence:* ${billing.payDate.toFormattedDate()} - *Saldo:* ${billing.rest.toPrint()}\n")
            }
        }

        sb.append("----------------------------\n")
        sb.append("Generado por MDCapp")
        return sb.toString()
    }
}
