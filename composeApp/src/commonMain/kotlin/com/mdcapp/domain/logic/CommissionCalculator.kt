package com.mdcapp.domain.logic

import com.mdcapp.domain.entities.CommissionConfig
import com.mdcapp.domain.entities.FactoryModel

object CommissionCalculator {

    /**
     * Calcula la comisión para un monto específico (pago).
     * 
     * @param amount El monto cobrado sobre el cual calcular.
     * @param factory La fábrica correspondiente para obtener las tasas.
     * @param branch El segmento/sucursal del documento.
     * @param docType El tipo de documento (Factura, Remito, etc.).
     * @param config Configuración global (ej: si descuenta IVA).
     */
    fun calculate(
        amount: Double,
        factory: FactoryModel,
        branch: String,
        docType: String,
        config: CommissionConfig = CommissionConfig()
    ): Double {
        // 1. Determinar la tasa de comisión (Prioridad: Segmento > Fábrica)
        val commissionRate = factory.segmentCommissions[branch]
            ?: factory.defaultCommission

        if (commissionRate <= 0.0) return 0.0

        // 2. Base imponible: Si es Factura y está configurado, descontar el IVA (21%)
        // Los Remitos (u otros tipos que no digan "Factura") no se descuentan.
        val taxableAmount =
            if (docType.contains("Factura", ignoreCase = true) && config.deductIVA) {
                amount / (1.0 + config.ivaRate)
            } else {
                amount
            }

        // 3. Resultado final
        return (taxableAmount * commissionRate) / 100.0
    }
}
