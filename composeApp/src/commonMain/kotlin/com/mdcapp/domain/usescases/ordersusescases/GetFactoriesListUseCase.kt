package com.mdcapp.domain.usescases.ordersusescases

import com.mdcapp.domain.remote.OrderRepository

class GetFactoriesListUseCase(private val repository: OrderRepository) {
    suspend operator fun invoke() = repository.getFactoriesList()
}