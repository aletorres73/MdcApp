package com.mdcapp.domain.usescases.homeusescases

import com.mdcapp.domain.entities.FactoryModel
import com.mdcapp.domain.repositories.HomeRepository

class HomeUseCase(private val repository: HomeRepository) {
    inner class GetAllFactories {
        suspend operator fun invoke(): List<FactoryModel> {
            return repository.getFactoriesList()
        }
    }
}
