package com.universitatcarlemany.unitutoring.ui

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.universitatcarlemany.unitutoring.MainActivity
import com.universitatcarlemany.unitutoring.database.AppDatabase
import com.universitatcarlemany.unitutoring.model.Reservation
import com.universitatcarlemany.unitutoring.ui.theme.UniTutoringTheme
import kotlinx.coroutines.launch

class ReservationActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissions(arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), 100)
        }

        val teacherName = intent.getStringExtra("teacherName") ?: "Profesor/a"
        val subject = intent.getStringExtra("subject") ?: ""

        setContent {
            UniTutoringTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    ReservationForm(teacherName, subject)
                }
            }
        }
    }

    @Composable
    fun ReservationForm(teacherName: String, subject: String) {
        val context = LocalContext.current
        val scope = rememberCoroutineScope()
        var date by remember { mutableStateOf("") }
        var time by remember { mutableStateOf("") }

        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Reservar tutoría con $teacherName",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            OutlinedTextField(
                value = date,
                onValueChange = { date = it },
                label = { Text("Fecha (ej. 2025-10-10)") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = time,
                onValueChange = { time = it },
                label = { Text("Hora (ej. 15:30)") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    // Validaciones
                    if (date.isBlank() || !Regex("""\d{4}-\d{2}-\d{2}""").matches(date)) {
                        Toast.makeText(context, "Introduce una fecha válida (ej. 2025-10-10)", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    if (time.isBlank() || !Regex("""\d{2}:\d{2}""").matches(time)) {
                        Toast.makeText(context, "Introduce una hora válida (ej. 15:30)", Toast.LENGTH_SHORT).show()
                        return@Button
                    }

                    val reservation = Reservation(
                        teacherName = teacherName,
                        subject = subject,
                        date = date,
                        time = time
                    )

                    val db = AppDatabase.getDatabase(context)
                    scope.launch {
                        db.reservationDao().insertReservation(reservation)
                        createNotificationChannel(context)
                        showReservationNotification(context, teacherName, date, time)
                        Toast.makeText(context, "Reserva guardada correctamente", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = "Confirmar reserva")
            }
        }
    }

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "reservation_channel",
                "Reservas",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notificaciones de nuevas reservas"
            }

            val manager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    fun showReservationNotification(
        context: Context,
        teacherName: String,
        date: String,
        time: String
    ) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent =
            PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE)

        val builder = NotificationCompat.Builder(context, "reservation_channel")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Reserva confirmada")
            .setContentText("Con $teacherName el $date a las $time")
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        NotificationManagerCompat.from(context).notify(1001, builder.build())
    }
}


