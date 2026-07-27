package com.mdcapp.ui.screens.invoicesClientDetail

import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import com.mdcapp.domain.entities.BillingModel
import com.mdcapp.domain.entities.toPrint
import com.mdcapp.ui.screens.invoicesClientDetail.common.CardHeader
import com.mdcapp.ui.screens.invoicesClientDetail.common.SectionCard
import com.mdcapp.ui.screens.invoicesClientDetail.common.ValueRow

@Composable
fun TotalsCard(billing: BillingModel) {

    SectionCard {
        val subtotal = billing.discount * billing.total

        CardHeader("Totales")

        ValueRow("Total", billing.total.toPrint())
        ValueRow("Descuento", subtotal.toPrint())

        HorizontalDivider()

        ValueRow(
            "A cobrar",
            billing.toPay.toPrint(),
            highlight = true
        )

        HorizontalDivider()

        ValueRow("Pagado", billing.payed.toPrint())
        ValueRow("Saldo", billing.rest.toPrint(), highlight = true)

    }
}
