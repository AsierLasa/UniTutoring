package com.universitatcarlemany.unitutoring.notifications

import android.content.Context
import androidx.work.*
import com.universitatcarlemany.unitutoring.workers.ReminderWorker
import java.time.*
import java.time.format.DateTimeFormatter

/**
 * Clase responsable de programar recordatorios de tutorías,
 * incluyendo recordatorios reales y pruebas de desarrollo.
 */
object NotificationScheduler {

    /**
     * Programa dos recordatorios automáticos para una tutoría:
     * uno 24 horas antes y otro 1 hora antes de la hora programada.
     *
     * @param context Contexto de la aplicación.
     * @param teacherName Nombre del profesor.
     * @param subject Asignatura de la tutoría.
     * @param dateString Fecha en formato "YYYY-MM-DD".
     * @param timeString Hora en formato "HH:mm".
     */
    fun scheduleReminders(
        context: Context,
        teacherName: String,
        subject: String,
        dateString: String,
        timeString: String
    ) {
        val date = LocalDate.parse(dateString, DateTimeFormatter.ISO_LOCAL_DATE)
        val time = LocalTime.parse(timeString, DateTimeFormatter.ofPattern("HH:mm"))
        val reservationDateTime = LocalDateTime.of(date, time)
        val now = LocalDateTime.now()

        val reminders = listOf(
            // 24 horas antes
            reservationDateTime.minusHours(24) to "Mañana tienes tu tutoría de $subject a las $timeString.",
            // 1 hora antes
            reservationDateTime.minusHours(1) to "¡Tu tutoría de $subject empieza en una hora!"
        )

        reminders.forEachIndexed { index, (reminderTime, message) ->
            if (reminderTime.isAfter(now)) {
                val delay = Duration.between(now, reminderTime)

                val inputData = Data.Builder()
                    .putString(ReminderWorker.KEY_TEACHER_NAME, teacherName)
                    .putString(ReminderWorker.KEY_SUBJECT, subject)
                    .putString(ReminderWorker.KEY_TIME, timeString)
                    .putString(ReminderWorker.KEY_MESSAGE, message)
                    .putInt("reminder_id", index)
                    .build()

                val workRequest = OneTimeWorkRequestBuilder<ReminderWorker>()
                    .setInitialDelay(delay)
                    .setInputData(inputData)
                    .build()

                WorkManager.getInstance(context).enqueue(workRequest)
            }
        }
    }

    /**
     * Programa una notificación de prueba para verificar el sistema de recordatorios.
     *
     * Esta notificación se lanza 10 segundos después de pulsar el botón.
     * Ideal para validar que el Worker, permisos y canal de notificaciones funcionan.
     *
     * @param context Contexto de la aplicación.
     */
    fun scheduleDebugReminder(context: Context) {
        val delay = Duration.ofSeconds(10)
        val now = LocalDateTime.now()

        val inputData = Data.Builder()
            .putString(ReminderWorker.KEY_TEACHER_NAME, "María Gómez")
            .putString(ReminderWorker.KEY_SUBJECT, "Matemáticas")
            .putString(ReminderWorker.KEY_TIME, now.plus(delay).format(DateTimeFormatter.ofPattern("HH:mm")))
            .putString(ReminderWorker.KEY_MESSAGE, "🔔 Notificación de prueba: así se verá un recordatorio real.")
            .build()

        val workRequest = OneTimeWorkRequestBuilder<ReminderWorker>()
            .setInitialDelay(delay)
            .setInputData(inputData)
            .build()

        WorkManager.getInstance(context).enqueue(workRequest)
    }
}
