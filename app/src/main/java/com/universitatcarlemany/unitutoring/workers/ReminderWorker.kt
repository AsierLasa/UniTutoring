package com.universitatcarlemany.unitutoring.workers

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.universitatcarlemany.unitutoring.MainActivity
import com.universitatcarlemany.unitutoring.R

/**
 * Worker que se encarga de mostrar una notificación recordatoria
 * sobre una tutoría programada.
 *
 * Este Worker se activa en el momento programado por WorkManager
 * (por ejemplo, 1h o 24h antes de una sesión), y muestra una notificación
 * en el sistema con un mensaje personalizado.
 */
class ReminderWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        val teacherName = inputData.getString(KEY_TEACHER_NAME) ?: return Result.failure()
        val subject = inputData.getString(KEY_SUBJECT) ?: return Result.failure()
        val time = inputData.getString(KEY_TIME) ?: return Result.failure()
        val customMessage = inputData.getString(KEY_MESSAGE)

        sendReminderNotification(teacherName, subject, time, customMessage)

        return Result.success()
    }

    /**
     * Muestra una notificación en el sistema recordando al usuario
     * su tutoría programada.
     *
     * @param teacherName Nombre del profesor.
     * @param subject Asignatura de la tutoría.
     * @param time Hora de la tutoría en formato HH:mm.
     * @param customMessage Mensaje personalizado opcional para mostrar.
     */
    private fun sendReminderNotification(
        teacherName: String,
        subject: String,
        time: String,
        customMessage: String?
    ) {
        createNotificationChannel(context)

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val content = customMessage
            ?: "¡No lo olvides! Tu tutoría de $subject con $teacherName es hoy a las $time."

        val builder = NotificationCompat.Builder(context, REMINDER_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Recordatorio de Tutoría")
            .setContentText(content)
            .setStyle(NotificationCompat.BigTextStyle().bigText(content))
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)

        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
            == PackageManager.PERMISSION_GRANTED
        ) {
            NotificationManagerCompat.from(context).notify(
                System.currentTimeMillis().toInt(),
                builder.build()
            )
        }
    }

    /**
     * Crea el canal de notificaciones necesario para mostrar
     * los recordatorios en versiones de Android 8.0 o superiores.
     *
     * @param context Contexto de la aplicación.
     */
    private fun createNotificationChannel(context: Context) {
        val name = "Recordatorios de Tutorías"
        val descriptionText = "Notificaciones para recordar tutorías programadas"
        val importance = NotificationManager.IMPORTANCE_HIGH
        val channel = NotificationChannel(REMINDER_CHANNEL_ID, name, importance).apply {
            description = descriptionText
        }
        val notificationManager: NotificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }

    companion object {
        const val KEY_TEACHER_NAME = "teacherName"
        const val KEY_SUBJECT = "subject"
        const val KEY_TIME = "time"
        const val KEY_MESSAGE = "customMessage"
        private const val REMINDER_CHANNEL_ID = "reminder_channel"
    }
}

