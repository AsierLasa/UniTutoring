package com.universitatcarlemany.unitutoring

import android.content.Intent
import android.widget.Toast
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.universitatcarlemany.unitutoring.database.AppDatabase
import com.universitatcarlemany.unitutoring.model.Reservation
import com.universitatcarlemany.unitutoring.notifications.NotificationScheduler
import com.universitatcarlemany.unitutoring.ui.ReservationListActivity
import com.universitatcarlemany.unitutoring.ui.TeacherListActivity
import com.universitatcarlemany.unitutoring.ui.theme.UniTutoringTheme
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver



class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            UniTutoringTheme {
                DashboardScreen(
                    onNavigateToNewReservation = {
                        startActivity(Intent(this, TeacherListActivity::class.java))
                    },
                    onNavigateToAllReservations = {
                        startActivity(Intent(this, ReservationListActivity::class.java))
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    onNavigateToNewReservation: () -> Unit,
    onNavigateToAllReservations: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val lifecycleOwner = LocalLifecycleOwner.current

    var nextReservation by remember { mutableStateOf<Reservation?>(null) }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                scope.launch {
                    val today = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
                    nextReservation = AppDatabase.getDatabase(context).reservationDao().getNextReservation(today)
                }
            }
        }

        val lifecycle = lifecycleOwner.lifecycle
        lifecycle.addObserver(observer)

        onDispose {
            lifecycle.removeObserver(observer)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("UniTutoring", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "¡Bienvenido/a!",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.primary
            )

            NextReservationCard(reservation = nextReservation)

            ActionButton(
                text = "Crear Nueva Reserva",
                icon = Icons.Default.Add,
                onClick = onNavigateToNewReservation
            )

            ActionButton(
                text = "Ver Todas Mis Reservas",
                icon = Icons.AutoMirrored.Filled.List,
                onClick = onNavigateToAllReservations,
                isPrimary = false
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Botón de prueba de notificación
            ActionButton(
                text = "📢 Probar Notificación",
                icon = Icons.Default.Notifications,
                onClick = {
                    NotificationScheduler.scheduleDebugReminder(context)
                    Toast.makeText(context, "Notificación de prueba programada. Aparecerá en 5 segundos.", Toast.LENGTH_LONG).show()
                },
                isPrimary = false
            )
        }
    }
}

@Composable
fun NextReservationCard(reservation: Reservation?) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.DateRange,
                    contentDescription = "Próxima tutoría",
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Tu Próxima Tutoría",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            if (reservation != null) {
                Text("Profesor/a: ${reservation.teacherName}", style = MaterialTheme.typography.bodyLarge)
                Text("Asignatura: ${reservation.subject}", style = MaterialTheme.typography.bodyLarge)
                Text("Fecha: ${reservation.date}", style = MaterialTheme.typography.bodyLarge)
                Text("Hora: ${reservation.time}", style = MaterialTheme.typography.bodyLarge)
            } else {
                Text(
                    "No tienes ninguna tutoría programada. ¡Anímate a reservar una!",
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
    }
}

@Composable
fun ActionButton(
    text: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    isPrimary: Boolean = true
) {
    val buttonColors = if (isPrimary) {
        ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
    } else {
        ButtonDefaults.outlinedButtonColors()
    }

    if (isPrimary) {
        Button(
            onClick = onClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            colors = buttonColors
        ) {
            Icon(imageVector = icon, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text(text, style = MaterialTheme.typography.labelLarge)
        }
    } else {
        OutlinedButton(
            onClick = onClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            Icon(imageVector = icon, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text(text, style = MaterialTheme.typography.labelLarge)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DashboardScreenPreview() {
    UniTutoringTheme {
        DashboardScreen({}, {})
    }
}
