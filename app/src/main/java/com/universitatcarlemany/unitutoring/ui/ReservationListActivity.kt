package com.universitatcarlemany.unitutoring.ui

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.universitatcarlemany.unitutoring.MainActivity
import com.universitatcarlemany.unitutoring.database.AppDatabase
import com.universitatcarlemany.unitutoring.model.Reservation
import com.universitatcarlemany.unitutoring.ui.theme.UniTutoringTheme
import kotlinx.coroutines.launch

class ReservationListActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            UniTutoringTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    ReservationListScreen()
                }
            }
        }
    }
}

@Composable
fun ReservationListScreen() {
    val context = LocalContext.current
    val db = AppDatabase.getDatabase(context)
    val scope = rememberCoroutineScope()

    var reservations by remember { mutableStateOf(listOf<Reservation>()) }

    LaunchedEffect(Unit) {
        reservations = db.reservationDao().getAllReservations()
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = "Reservas realizadas",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        items(reservations) { reservation ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight(),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Profesor: ${reservation.teacherName}")
                    Text("Asignatura: ${reservation.subject}")
                    Text("Fecha: ${reservation.date}")
                    Text("Hora: ${reservation.time}")
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = {
                    val intent = Intent(context, MainActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                    context.startActivity(intent)
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Volver al menú")
            }
        }
    }
}

