package com.example.franco_rojas_seccioncurso

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.work.Worker
import androidx.work.WorkerParameters

class CalculatorWorker(context: Context, workerParams: WorkerParameters) : Worker(context, workerParams) {
    override fun doWork(): Result {
        val operationType = inputData.getString("operation") ?: return Result.failure()
        val number = inputData.getString("number") ?: return Result.failure()

        return try {
            val result = when (operationType) {
                "toBinary" -> number.toInt().toString(2)
                "toDecimal" -> Integer.parseInt(number, 2).toString()
                "add" -> {
                    val numbers = number.split("+").map { it.trim().toInt(2) }
                    numbers.sum().toString(2)
                }
                "subtract" -> {
                    val numbers = number.split("-").map { it.trim().toInt(2) }
                    if (numbers.size == 2) (numbers[0] - numbers[1]).toString(2) else "Error"
                }
                else -> "Operación no válida"
            }
            sendNotification("Resultado de $operationType", result)
            Result.success(androidx.work.Data.Builder().putString("result", result).build())
        } catch (e: Exception) {
            Result.failure()
        }
    }

    private fun sendNotification(title: String, message: String) {
        val channelId = "calculator_channel"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Calculator Notifications",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val manager = applicationContext.getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(applicationContext, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        // Verificar el permiso antes de enviar la notificación
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(applicationContext, Manifest.permission.POST_NOTIFICATIONS) == android.content.pm.PackageManager.PERMISSION_GRANTED) {
            NotificationManagerCompat.from(applicationContext).notify(System.currentTimeMillis().toInt(), notification)
        }
    }
}
