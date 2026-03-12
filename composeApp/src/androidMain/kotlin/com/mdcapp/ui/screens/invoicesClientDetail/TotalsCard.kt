package com.mdcapp.ui.screens.invoicesClientDetail

import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import com.mdcapp.data.model.BillingModel
import com.mdcapp.data.model.toPrint
import com.mdcapp.ui.screens.invoicesClientDetail.common.CardHeader
import com.mdcapp.ui.screens.invoicesClientDetail.common.SectionCard
import com.mdcapp.ui.screens.invoicesClientDetail.common.ValueRow

@Composable
fun TotalsCard(billing: BillingModel) {

    SectionCard {

        CardHeader("Totales")

        ValueRow("Total", billing.total.toPrint())
        ValueRow("Descuento", billing.discount.toPrint())

        HorizontalDivider()

        ValueRow(
            "A cobrar",
            (billing.total - billing.discount).toPrint(),
            highlight = true
        )

        HorizontalDivider()

        ValueRow("Pagado", billing.payed.toPrint())
        ValueRow("Saldo", billing.rest.toPrint(), highlight = true)

    }
}