package com.mdcapp.domain.usescases.homeusescases

import com.mdcapp.data.model.FactoryModel
import com.mdcapp.domain.remote.HomeRepository

class HomeUseCase(private val repository: HomeRepository) {
    inner class GetAllFactories {
        suspend operator fun invoke(): List<FactoryModel> {
            return repository.getFactoriesList()
        }
    }
}