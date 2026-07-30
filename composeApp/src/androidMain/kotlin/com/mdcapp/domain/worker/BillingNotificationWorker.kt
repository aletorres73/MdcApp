package com.mdcapp.domain.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.mdcapp.domain.entities.toLocalDate
import com.mdcapp.domain.repositories.OrderRepository
import kotlinx.coroutines.flow.first
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.time.LocalDate
import java.time.temporal.ChronoUnit

class BillingNotificationWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params), KoinComponent {

    private val repository: OrderRepository by inject()

    companion object {
        const val CHANNEL_ID = "billing_alerts"
        const val CHANNEL_NAME = "Alertas de Facturación"
    }

    override suspend fun doWork(): Result {
        try {
            val billings = repository.observeAllBillings().first()
            val today = LocalDate.now()

            billings.forEach { billing ->
                if (billing.rest > 0 && billing.payDate != 0L) {
                    val payDate = billing.payDate.toLocalDate()
                    val daysUntilDue = ChronoUnit.DAYS.between(today, payDate)

                    when {
                        daysUntilDue == 0L -> {
                            sendNotification(
                                "⚠️ Factura Vence Hoy",
                                "La factura ${billing.billingNumber} de ${billing.clientName} vence hoy."
                            )
                        }

                        daysUntilDue in 1L..2L -> {
                            sendNotification(
                                "🟡 Factura por Vencer",
                                "La factura ${billing.billingNumber} de ${billing.clientName} vence en $daysUntilDue días."
                            )
                        }

                        daysUntilDue < 0 -> {
                            // Opcional: Notificar si ya venció y no se ha cobrado
                            sendNotification(
                                "🔴 Factura Vencida",
                                "La factura ${billing.billingNumber} de ${billing.clientName} está vencida."
                            )
                        }
                    }
                }
            }
            return Result.success()
        } catch (e: Exception) {
            return Result.failure()
        }
    }

    private fun sendNotification(title: String, message: String) {
        val notificationManager =
            applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationManager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info) // Reemplazar por icono de la app si existe
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(System.currentTimeMillis().toInt(), notification)
    }
}
