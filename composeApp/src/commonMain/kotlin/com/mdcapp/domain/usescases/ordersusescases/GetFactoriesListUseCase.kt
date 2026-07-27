package com.mdcapp.domain.usescases.ordersusescases

import com.mdcapp.domain.repositories.OrderRepository

class GetFactoriesListUseCase(private val repository: OrderRepository) {
    suspend operator fun invoke() = repository.getFactoriesList()
}
