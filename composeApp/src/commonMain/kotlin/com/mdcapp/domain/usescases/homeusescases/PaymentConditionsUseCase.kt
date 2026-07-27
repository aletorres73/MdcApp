package com.mdcapp.domain.usescases.homeusescases

import com.mdcapp.domain.entities.PaymentCondition
import com.mdcapp.domain.repositories.OrderRepository

class PaymentConditionsUseCase(private val repository: OrderRepository) {
    inner class GetPaymentsConditions {
        suspend operator fun invoke(factoryName: String): List<PaymentCondition> {
            return repository.getPaymentsConditionFactory(factoryName)
        }
    }

    inner class SetPaymentsConditionsFactory {
        suspend operator fun invoke(factoryName: String, data: List<PaymentCondition>): Boolean {
            return repository.addPaymentConditionsToFactory(factoryName, data)
        }
    }
}
