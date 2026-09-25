package com.mdcapp.domain.usescases.commissionusecase

import com.mdcapp.domain.entities.BillingModel
import com.mdcapp.domain.entities.CommissionConfig
import com.mdcapp.domain.entities.FactoryModel
import com.mdcapp.domain.entities.MovementMethod
import com.mdcapp.domain.entities.PaymentRegisterModel
import com.mdcapp.domain.logic.CommissionCalculator
import com.mdcapp.domain.repositories.OrderRepository

class CommissionUseCase(private val repository: OrderRepository) {

    data class CommissionResult(
        val payment: PaymentRegisterModel,
        val billing: BillingModel,
        val commission: Double
    )

    inner class GetCommissions {
        suspend operator fun invoke(
            startDate: Long,
            endDate: Long,
            config: CommissionConfig = CommissionConfig()
        ): List<CommissionResult> {
            // 1. Obtener pagos en rango (startDate y endDate ya están ajustados a inicio y fin de día local)
            val payments = repository.fetchPaymentsByDateRange(startDate, endDate)
                .filter { !MovementMethod.fromName(it.method).isVirtual }

            if (payments.isEmpty()) return emptyList()

            // 2. Obtener facturas asociadas en batches
            val billingsNumbers = payments.map { it.documentNumber }.distinct()
            val billings = repository.fetchBillingsByNumbers(billingsNumbers)

            // 3. Obtener fábricas para las tasas
            val factories = repository.getFactories()

            // 4. Calcular comisiones
            return payments.mapNotNull { payment ->
                val billing = billings.find { it.billingNumber == payment.documentNumber }
                if (billing != null) {
                    val factory =
                        factories.find { it.name.equals(billing.brand, ignoreCase = true) }
                            ?: FactoryModel(billing.brand, emptyList(), emptyList())

                    val commission = CommissionCalculator.calculate(
                        amount = payment.total,
                        factory = factory,
                        branch = billing.branch,
                        docType = billing.type,
                        config = config
                    )
                    CommissionResult(payment, billing, commission)
                } else null
            }
        }
    }
}
