package com.mdcapp.domain.usescases.homeusescases

import com.mdcapp.data.model.PaymentCondition
import com.mdcapp.domain.remote.OrderRepository

class PaymentConditionsUseCase(private val repository: OrderRepository) {
    inner class GetPaymentsConditions {
        suspend operator fun invoke(factoryName: String): List<PaymentCondition> {
            return repository.getPaymentsConditionFactory(factoryName)
        }
    }
}