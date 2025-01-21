package com.mdcapp.data.model

data class FactoryModel(
    val name: String,
    val branchList: List<String>,
    val paymentType: List<PaymentCondition>
)

fun FactoryModel.printPaymentType() {
    paymentType.forEach { paymentType ->
        println(paymentType)
    }
}